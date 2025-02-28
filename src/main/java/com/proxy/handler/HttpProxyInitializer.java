package com.proxy.handler;

import com.proxy.service.impl.AccessControlServiceImpl;
import com.proxy.service.impl.TrafficStatsServiceImpl;
import com.proxy.service.impl.UserServiceImpl;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

public class HttpProxyInitializer extends ChannelInitializer<SocketChannel> {
    private final UserServiceImpl userService;
    private final AccessControlServiceImpl accessControlService;
    private final TrafficStatsServiceImpl trafficStatsService;

    public HttpProxyInitializer(UserServiceImpl userService, 
                               AccessControlServiceImpl accessControlService,
                               TrafficStatsServiceImpl trafficStatsService) {
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