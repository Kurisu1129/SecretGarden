package com.proxy.handler;

import com.proxy.service.IAccessControlService;
import com.proxy.service.ITrafficStatsService;
import com.proxy.service.IUserService;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

public class HttpProxyInitializer extends ChannelInitializer<SocketChannel> {
    private final IUserService userService;
    private final IAccessControlService accessControlService;
    private final ITrafficStatsService trafficStatsService;

    public HttpProxyInitializer(
            IUserService userService,
            IAccessControlService accessControlService,
            ITrafficStatsService trafficStatsService) {
        this.userService = userService;
        this.accessControlService = accessControlService;
        this.trafficStatsService = trafficStatsService;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline()
            .addLast(new HttpServerCodec())
            .addLast(new AuthHandler(userService))
            .addLast(new HttpProxyHandler(userService, accessControlService, trafficStatsService));
    }
}