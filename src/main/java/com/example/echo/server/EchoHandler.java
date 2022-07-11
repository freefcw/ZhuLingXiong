package com.example.echo.server;

import com.example.client.proto.Echo;
import com.example.echo.EchoActionType;
import com.example.gateway.server.message.MessageFactory;
import com.example.gateway.server.message.internal.InternalMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ChannelHandler.Sharable
public class EchoHandler extends SimpleChannelInboundHandler<InternalMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, InternalMessage inMessage) throws InvalidProtocolBufferException {
        log.info("receive message from user {}", inMessage.userId());

        Echo.NewEchoMessage message = Echo.NewEchoMessage.parseFrom(inMessage.payload().nioBuffer());
        log.info("client message is: {}", message.getContent());

        InternalMessage outMessage = MessageFactory.make(EchoActionType.ECHO_RESPONSE.id(), inMessage.userId());

        Echo.EchoResponse.Builder builder = Echo.EchoResponse.newBuilder();
        builder.setTs((int) (System.currentTimeMillis() / 1000));
        builder.setContent(message.getContent() + " BACK FROM SERVER");
        outMessage.setPayload(builder.build());


        channelHandlerContext.channel().writeAndFlush(outMessage);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel {} active", ctx.channel().id().asShortText());

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel {} inactive", ctx.channel().id().asShortText());

        super.channelInactive(ctx);
    }
}
