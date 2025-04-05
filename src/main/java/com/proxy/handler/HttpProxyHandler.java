package com.proxy.handler;

import com.proxy.service.IAccessControlService;
import com.proxy.service.ITrafficStatsService;
import com.proxy.service.IUserService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpProxyHandler extends ChannelInboundHandlerAdapter {
    private final IUserService userService;
    private final IAccessControlService accessControlService;
    private final ITrafficStatsService trafficStatsService;

    public HttpProxyHandler(
            IUserService userService,
            IAccessControlService accessControlService,
            ITrafficStatsService trafficStatsService) {
        this.userService = userService;
        this.accessControlService = accessControlService;
        this.trafficStatsService = trafficStatsService;
    }
    private static final Logger logger = LogManager.getLogger(HttpProxyHandler.class);

    private Channel outboundChannel;
    private String currentHost;
    private long bytesUp;
    private long bytesDown;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String host = request.headers().get(HttpHeaderNames.HOST);
            if (host == null) {
                logger.error("No host header in HTTP request");
                ctx.close();
                return;
            }

            // 获取认证用户
            AuthHandler authHandler = ctx.pipeline().get(AuthHandler.class);
            String username = authHandler.getAuthenticatedUser();
            if (username == null) {
                logger.error("Unauthenticated request");
                ctx.close();
                return;
            }

            // 检查访问控制
            Long userId = userService.getUserId(username);
            if (userId == null || !accessControlService.isAllowed(userId, host)) {
                logger.error("Access denied for user {} to host {}", username, host);
                sendErrorResponse(ctx, HttpResponseStatus.FORBIDDEN);
                return;
            }

            String[] hostParts = host.split(":");
            currentHost = hostParts[0];
            int port = hostParts.length > 1 ? Integer.parseInt(hostParts[1]) : 80;

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop())
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                .addLast(new HttpClientCodec())
                                .addLast(new TrafficCountHandler())
                                .addLast(new RelayHandler(ctx.channel()));
                        }
                    });

            ChannelFuture f = bootstrap.connect(currentHost, port);
            outboundChannel = f.channel();
            f.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.channel().read();
                    outboundChannel.writeAndFlush(request);
                } else {
                    logger.error("Failed to connect to target server", future.cause());
                    ctx.close();
                }
            });
        } else if (msg instanceof HttpContent) {
            if (outboundChannel != null && outboundChannel.isActive()) {
                ByteBuf content = ((HttpContent) msg).content();
                bytesUp += content.readableBytes();
                outboundChannel.writeAndFlush(msg);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (outboundChannel != null) {
            // 记录流量统计
            AuthHandler authHandler = ctx.pipeline().get(AuthHandler.class);
            String username = authHandler.getAuthenticatedUser();
            if (username != null && currentHost != null) {
                Long userId = userService.getUserId(username);
                if (userId != null) {
                    trafficStatsService.recordTraffic(userId, currentHost, bytesUp, bytesDown);
                }
            }
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught in HttpProxyHandler", cause);
        closeOnFlush(ctx.channel());
    }

    private static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void sendErrorResponse(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, status,
            Unpooled.copiedBuffer(status.toString(), java.nio.charset.StandardCharsets.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private class TrafficCountHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof HttpContent) {
                ByteBuf content = ((HttpContent) msg).content();
                bytesDown += content.readableBytes();
            }
            ctx.fireChannelRead(msg);
        }
    }
}