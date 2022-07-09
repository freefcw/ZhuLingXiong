package com.example.gateway.upstream.forward;

import com.example.gateway.server.handler.MessageDecoder;
import com.example.gateway.server.handler.MessageEncoder;
import com.example.gateway.upstream.common.ConnectionManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ForwardChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final ConnectionManager connectionManager;
    private final MessageEncoder messageEncoder;

    public ForwardChannelInitializer(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.messageEncoder = new MessageEncoder();
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new MessageDecoder());
        pipeline.addLast(this.messageEncoder);

        pipeline.addLast(new ForwardMessageHandler(this.connectionManager));
    }
}
