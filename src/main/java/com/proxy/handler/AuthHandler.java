package com.proxy.handler;

import com.proxy.service.IUserService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private final IUserService userService;

    private String authenticatedUser;

    public AuthHandler(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            String auth = request.headers().get(HttpHeaderNames.PROXY_AUTHORIZATION);
            
            if (auth != null && auth.startsWith("Basic ")) {
                String credentials = new String(Base64.getDecoder().decode(
                    auth.substring(6)), StandardCharsets.UTF_8);
                String[] parts = credentials.split(":", 2);
                
                if (parts.length == 2) {
                    String username = parts[0];
                    String password = parts[1];
                    
                    if (userService.authenticate(username, password)) {
                        authenticatedUser = username;
                        ctx.fireChannelRead(msg);
                        return;
                    }
                }
            }
            
            // 认证失败，发送407响应
            HttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED);
            response.headers().set(HttpHeaderNames.PROXY_AUTHENTICATE, "Basic realm=\"Proxy Authentication Required\"");
            ctx.writeAndFlush(response);
        } else {
            // 非HTTP请求直接传递
            ctx.fireChannelRead(msg);
        }
    }

    public String getAuthenticatedUser() {
        return authenticatedUser;
    }
}