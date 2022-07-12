package com.example.echo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ConnectionFactory {
    private Bootstrap bootstrap;

    private EventLoopGroup eventLoopGroup;

    public ConnectionFactory() {
        this.init();
    }

    public void init() {
        this.bootstrap = new Bootstrap();
        int maxThreads = Runtime.getRuntime().availableProcessors();
        this.eventLoopGroup = new NioEventLoopGroup(maxThreads);
        this.bootstrap.group(this.eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new EchoClientInitializer());
    }

    public ChannelFuture create(String host, Integer port) {
        if (this.bootstrap == null) {
            log.error("bootstrap is null!");
            throw new RuntimeException();
        }
        ChannelFuture future = this.bootstrap.connect(new InetSocketAddress(host, port));
        future.addListener(future1 -> {
            if (future1.isSuccess()) {
                log.info("connect {}:{} success", host, port);
            } else {
                log.error("connect {}:{} failed {}, will terminal...", host, port, future1.cause().getMessage());
                this.close();
            }
        });

        future.channel().closeFuture().addListener(future1 -> {
            log.info("closing future");
        });

        return future;
    }

    public void close() {
        // eventLoop shutdown then program can exit
        this.eventLoopGroup.shutdownGracefully();
    }

}
