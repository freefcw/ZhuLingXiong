package com.example.gateway.upstream.common;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

public class PoolConnectionFactory {
    private final Bootstrap bootstrap;
    private FixedChannelPool channelPool;

    public PoolConnectionFactory() {
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class);
    }

    public void configureInitializer(ChannelPoolHandler channelPoolHandler, Integer concurrency) {
        this.channelPool = new FixedChannelPool(this.bootstrap, channelPoolHandler, concurrency);
    }

    public void setRemoteInfo(String host, Integer port) {
        this.bootstrap.remoteAddress(host, port);
    }

    public Future<Channel> acquire() {
        return this.channelPool.acquire();
    }

    public void release(Channel channel) {
        this.channelPool.release(channel);
    }

    public void shutdown() {
        this.channelPool.close();
    }
}
