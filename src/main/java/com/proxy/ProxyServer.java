package com.proxy;

import com.proxy.handler.HttpProxyInitializer;
import com.proxy.handler.SocksProxyInitializer;
import com.proxy.service.IUserService;
import com.proxy.service.IAccessControlService;
import com.proxy.service.ITrafficStatsService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.CompletableFuture;

@Component
public class ProxyServer {
    @Value("${proxy.http.port}")
    private int httpPort;

    @Value("${proxy.socks.port}")
    private int socksPort;

    @Autowired
    private IUserService userService;

    @Autowired
    private IAccessControlService accessControlService;

    @Autowired
    private ITrafficStatsService trafficStatsService;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture httpChannelFuture;
    private ChannelFuture socksChannelFuture;

    @PostConstruct
    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        CompletableFuture.allOf(
            startHttpProxy(),
            startSocksProxy()
        ).join();
    }

    private CompletableFuture<Void> startHttpProxy() {
        return CompletableFuture.runAsync(() -> {
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HttpProxyInitializer(userService, accessControlService, trafficStatsService));

                httpChannelFuture = b.bind(httpPort).sync();
            } catch (Exception e) {
                throw new RuntimeException("Failed to start HTTP proxy", e);
            }
        });
    }

    private CompletableFuture<Void> startSocksProxy() {
        return CompletableFuture.runAsync(() -> {
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new SocksProxyInitializer());

                socksChannelFuture = b.bind(socksPort).sync();
            } catch (Exception e) {
                throw new RuntimeException("Failed to start SOCKS proxy", e);
            }
        });
    }

    @PreDestroy
    public void stop() {
        try {
            if (httpChannelFuture != null) {
                httpChannelFuture.channel().close().sync();
            }
            if (socksChannelFuture != null) {
                socksChannelFuture.channel().close().sync();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}