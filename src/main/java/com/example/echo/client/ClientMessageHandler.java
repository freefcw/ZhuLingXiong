package com.example.echo.client;

import com.example.client.proto.Echo;
import com.example.echo.EchoActionType;
import com.example.gateway.module.gateway.GeneralActionType;
import com.example.gateway.proto.General;
import com.example.gateway.server.message.BaseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@ChannelHandler.Sharable
@Slf4j
public class ClientMessageHandler extends SimpleChannelInboundHandler<BaseMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseMessage msg) throws Exception {
        log.info("receive command: {}", msg.commandId());
        if (msg.commandId() == EchoActionType.NEW_ECHO_REQUEST.id()) {
            Echo.NewEchoMessage chatMessage = Echo.NewEchoMessage.parseFrom(msg.payload().nioBuffer());
            log.info("receive message: {}", chatMessage.getContent());
        }
        if (msg.commandId() == GeneralActionType.Auth_Result.id()) {
            General.LoginResponse authResult = General.LoginResponse.parseFrom(msg.payload().nioBuffer());

            log.info("auth result: {}", authResult.getResult());
            if (authResult.getResult() == 0) {
                ctx.channel().attr(SessionKey.USER_ID).set(authResult.getUserId());
                log.info("auth success!");
            } else {
                log.info("auth failed!");
            }
            synchronized (ctx.channel()) {
                ctx.channel().notify();
            }
        }
        if (msg.commandId() == EchoActionType.ECHO_RESPONSE.id()) {
            Echo.EchoResponse echoResponse = Echo.EchoResponse.parseFrom(msg.payload().nioBuffer());
            log.info("receive message: {} @ {}", echoResponse.getContent(), echoResponse.getTs());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("ClientMessageHandler active {}", ctx.channel().id().asShortText());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("channel {} inactive", ctx.channel().id().asShortText());
        super.channelInactive(ctx);
    }
}
