package com.example.gateway.upstream.common;

import com.example.gateway.server.message.BaseMessage;
import com.example.gateway.server.session.NetSession;
import io.netty.channel.ChannelHandlerContext;

public interface ConnectionManager {
    void writeAndFlush(NetSession session, BaseMessage message) throws InterruptedException;

    void inactive(ChannelHandlerContext ctx);

    void shutdown();
}
