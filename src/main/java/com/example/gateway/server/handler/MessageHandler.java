package com.example.gateway.server.handler;

import com.example.gateway.module.gateway.GeneralActionType;
import com.example.gateway.proto.General;
import com.example.gateway.server.cocurrent.ActionExecutor;
import com.example.gateway.server.logic.LogicProcessor;
import com.example.gateway.server.message.BaseMessage;
import com.example.gateway.server.session.NetSession;
import com.example.gateway.server.session.SessionManager;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@ChannelHandler.Sharable
public class MessageHandler extends SimpleChannelInboundHandler<BaseMessage> {

    private final SessionManager sessionManager;
    private final LogicProcessor logicProcessor;

    public MessageHandler(SessionManager sessionManager, LogicProcessor logicProcessor) {
        this.sessionManager = sessionManager;
        this.logicProcessor = logicProcessor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, BaseMessage message) {
        if (!context.channel().hasAttr(NetSession.sessionKey())) {
            log.error("session is not found! {}", context.channel().id().asShortText());
            return;
        }

        Attribute<NetSession> sessionAttr = context.channel().attr(NetSession.sessionKey());
        NetSession session = sessionAttr.get();
        log.info("catch message: {}", message);
        if (message.commandId() == GeneralActionType.HeartBeat.id()) {
            try {
                General.HeartBeat heartBeat = General.HeartBeat.parseFrom(message.payload().nioBuffer());
                log.info("heartbeat message: userId: {}, type: {}", heartBeat.getUserId(), heartBeat.getType());
                session.setUserId(heartBeat.getType());
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        if (session.isAuthenticated() || this.isLogin(message)) {
            logicProcessor.addRequest(new ActionExecutor(session, message));
            return;
        }
        log.warn("channel {} has message on anonymous {}", context.channel().id().asShortText(), message.commandId());
        this.sessionManager.inactive(context.channel());
    }

    private boolean isLogin(BaseMessage message) {
        return message.commandId() == GeneralActionType.Auth_Login.id();
    }

    /**
     * 超时处理 如果5秒没有接受客户端的心跳，就触发; 如果超过两次，则直接关闭;
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (obj instanceof IdleStateEvent) {
//            handleIdleEvent(ctx, (IdleStateEvent) obj);
        } else {
            super.userEventTriggered(ctx, obj);
        }
    }

    private void handleIdleEvent(ChannelHandlerContext ctx, IdleStateEvent obj) {
        // 如果读通道处于空闲状态，说明没有接收到心跳命令
        if (IdleState.READER_IDLE.equals(obj.state())) {
            log.debug("5s heartbeat");
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("channel {}, client connected ! {}", ctx.channel().id().asShortText(), ctx.channel().remoteAddress());
        this.sessionManager.newSession(ctx.channel());

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Channel({}) disconnected! RemoteAddress: {}",
                ctx.channel().id().asShortText(),
                ctx.channel().remoteAddress()
        );
        this.sessionManager.inactive(ctx.channel());

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

}
