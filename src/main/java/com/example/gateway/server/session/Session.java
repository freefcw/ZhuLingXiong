package com.example.gateway.server.session;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import java.util.UUID;


public interface Session extends AutoCloseable {

    UUID id();

    void setUserId(Integer userId);

    Integer userId();

    Channel channel();

    boolean isActive();

    void writeAndFlush(Object msg);

    void writeAndFlush(Object message, ChannelFutureListener listener);

    void close();

    boolean isAuthenticated();
}