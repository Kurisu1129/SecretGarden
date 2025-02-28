package com.proxy.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.v4.Socks4ServerDecoder;
import io.netty.handler.codec.socksx.v4.Socks4ServerEncoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;

public class SocksProxyInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline()
            .addLast(new Socks4ServerDecoder())
            .addLast(Socks4ServerEncoder.INSTANCE)
            .addLast(Socks5ServerEncoder.DEFAULT)
            .addLast(new SocksPortUnificationServerHandler())
            .addLast(new SocksProxyHandler());
    }
} 