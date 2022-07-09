package com.example.gateway.upstream.forward;

import com.example.gateway.server.message.BaseMessage;
import com.example.gateway.server.session.NetSession;
import com.example.gateway.upstream.common.ConnectionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

/**
 * forward mode
 * forward request to internal service
 */
@Slf4j
@ChannelHandler.Sharable
public class ForwardMessageHandler extends SimpleChannelInboundHandler<BaseMessage> {
    private final ConnectionManager connectionManager;

    public ForwardMessageHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BaseMessage msg) {
        log.debug("forward mode receive upstream message {}, ", msg.commandId());
        if (ctx.channel().hasAttr(NetSession.sessionKey())) {
            Attribute<NetSession> attr = ctx.channel().attr(NetSession.sessionKey());
            NetSession session = attr.get();
            log.debug("receive message {}, forward to user {}", msg.commandId(), session.userId());
            session.writeAndFlush(msg);
        } else {
            log.error("Channel {} without session! msg id {}", ctx.channel().id().asShortText(), msg.commandId());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("ClientMessageHandler active {}", ctx.channel().id().asShortText());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel {} inactive", ctx.channel().id().asShortText());
        this.connectionManager.inactive(ctx);
        super.channelInactive(ctx);
    }
}
