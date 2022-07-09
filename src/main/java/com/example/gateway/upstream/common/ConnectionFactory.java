package com.example.gateway.upstream.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConnectionFactory {
    public static final int MAX_IDLE_SECONDS = 5;

    private final Bootstrap bootstrap;

    private final AtomicInteger retiesCount = new AtomicInteger(0);
    private long lastTriedTime;

    public ConnectionFactory() {
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class);
        this.bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 500);
    }

    public void configureInitializer(ChannelInitializer<SocketChannel> initializer) {
        this.bootstrap.handler(initializer);
    }

    public void setRemoteInfo(String host, Integer port) {
        this.bootstrap.remoteAddress(host, port);
    }

    public ChannelFuture acquire() throws InterruptedException {
        if (this.inGapTime()) {
            return null;
        }
        ChannelFuture future = this.bootstrap.connect();
        log.debug("try to connect remote {}", future.channel().remoteAddress());
        future.addListener(future1 -> {
            if (future1.isSuccess()) {
                if (this.retiesCount.get() != 0) {
                    this.retiesCount.set(0);
                }
                log.debug("connect to {} success", future.channel().remoteAddress());
            } else {
                this.retiesCount.incrementAndGet();
                log.error("try to connect {} times {}, failed {}", future.channel().remoteAddress(), this.retiesCount, future1.cause().getMessage());
                this.lastTriedTime = System.currentTimeMillis();
            }
        });
        return future;
    }

    private boolean inGapTime() {
        if (this.retiesCount.get() == 0) {
            return false;
        }

        int idleSeconds = Math.min(this.retiesCount.get(), MAX_IDLE_SECONDS);
        long gapEndTime = idleSeconds * 1000L + this.lastTriedTime;
        log.info("gap time test retries {} {} {}", this.retiesCount.get(), this.lastTriedTime, gapEndTime);
        return System.currentTimeMillis() < gapEndTime;
    }

    public void release(Channel channel) {
        if (channel != null) {
            channel.close();
        }
    }
}
