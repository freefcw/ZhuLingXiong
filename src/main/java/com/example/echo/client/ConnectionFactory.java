package com.example.echo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ConnectionFactory {
    private Bootstrap bootstrap;

    public ConnectionFactory() {
        this.init();
    }

    public void init() {
        this.bootstrap = new Bootstrap();
        int maxThreads = Runtime.getRuntime().availableProcessors();
        bootstrap.group(new NioEventLoopGroup(maxThreads))
                .channel(NioSocketChannel.class)
                .handler(new EchoClientInitializer());
    }

    public ChannelFuture create() {
        String host = "192.168.10.200";
//        String host = "127.0.0.1";
//        Integer port = 9922;
        Integer port = 7890;
        return this.create(host, port);
    }

    public ChannelFuture create(String host, Integer port) {
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
        future.addListener(future1 -> {
            if (future1.isSuccess()) {
                log.info("connect {}:{} success", host, port);
            } else {
                log.error("connect {}:{} failed {}", host, port, future1.cause().getMessage());
            }
        });
        return future;
    }
}
