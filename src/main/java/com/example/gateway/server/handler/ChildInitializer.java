package com.example.gateway.server.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ChildInitializer extends ChannelInitializer<SocketChannel> {

    private final MessageHandler messageHandler;
    private final BlackListHandler blackListHandler;

    public ChildInitializer(MessageHandler messageHandler, BlackListHandler blackListHandler) {
        this.messageHandler = messageHandler;
        this.blackListHandler = blackListHandler;
    }


    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addFirst(this.blackListHandler);
        // parameters: read timeout, write timeout, all type, timeunit
        pipeline.addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));

        pipeline.addLast(new MessageDecoder());
        pipeline.addLast(new MessageEncoder());

        pipeline.addLast(this.messageHandler);
    }

}
