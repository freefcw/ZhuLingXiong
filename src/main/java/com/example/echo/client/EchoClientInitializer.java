package com.example.echo.client;

import com.example.gateway.server.handler.MessageDecoder;
import com.example.gateway.server.handler.MessageEncoder;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoClientInitializer extends io.netty.channel.ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new MessageDecoder());
        pipeline.addLast(new MessageEncoder());

        pipeline.addLast(new ClientMessageHandler());
    }
}
