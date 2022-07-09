package com.example.gateway.upstream.common;

import com.example.gateway.server.handler.internal.MessageDecoder;
import com.example.gateway.server.handler.internal.MessageEncoder;
import com.example.gateway.server.session.SessionManager;
import com.example.gateway.upstream.proxy.ProxyMessageHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.ChannelPoolHandler;
import lombok.extern.slf4j.Slf4j;

@ChannelHandler.Sharable
@Slf4j
public class PooledChannelHandler implements ChannelPoolHandler {
    private final MessageEncoder messageEncoder;
    private final ProxyMessageHandler messageHandler;

    public PooledChannelHandler(ConnectionManager connectionManager, SessionManager sessionManager) {
        this.messageEncoder = new MessageEncoder();
        if (sessionManager == null) {
            log.info("here is null session manager");
            System.exit(1);
        }
        this.messageHandler = new ProxyMessageHandler(connectionManager, sessionManager);
    }

    @Override
    public void channelReleased(Channel ch) throws Exception {

    }

    @Override
    public void channelAcquired(Channel ch) throws Exception {

    }

    @Override
    public void channelCreated(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new MessageDecoder());
        pipeline.addLast(this.messageEncoder);
        pipeline.addLast(this.messageHandler);
    }
}
