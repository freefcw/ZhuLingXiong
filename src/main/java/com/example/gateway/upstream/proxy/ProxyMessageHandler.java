package com.example.gateway.upstream.proxy;

import com.example.gateway.server.message.MessageConverter;
import com.example.gateway.server.message.internal.InternalMessage;
import com.example.gateway.server.session.NetSession;
import com.example.gateway.server.session.SessionManager;
import com.example.gateway.upstream.common.ConnectionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * proxy mode
 * transform client request to internal request
 */
@Slf4j
@ChannelHandler.Sharable
public class ProxyMessageHandler extends SimpleChannelInboundHandler<InternalMessage> {
    private final ConnectionManager connectionManager;
    private final SessionManager sessionManager;

    public ProxyMessageHandler(ConnectionManager connectionManager, SessionManager sessionManager) {
        this.connectionManager = connectionManager;
        this.sessionManager = sessionManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InternalMessage msg) {
        log.debug("proxy mode receive upstream message {}, ", msg.commandId());
        Optional<NetSession> optional = this.sessionManager.get(msg.userId());
        if (optional.isEmpty()) {
            log.error("Channel {} without session! msg id {}", ctx.channel().id().asShortText(), msg.commandId());
            return;
        }
        NetSession session = optional.get();
        session.writeAndFlush(MessageConverter.toBaseMessage(msg));
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
