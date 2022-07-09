package com.example.gateway.upstream.forward;

import com.example.gateway.server.message.BaseMessage;
import com.example.gateway.server.session.NetSession;
import com.example.gateway.server.session.SessionClosedEvent;
import com.example.gateway.server.session.SessionInactiveEvent;
import com.example.gateway.upstream.common.ConnectionFactory;
import com.example.gateway.upstream.common.ConnectionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ForwardConnectionManager implements ConnectionManager {
    private final static ConcurrentHashMap<Integer, Channel> USER_CHANNEL = new ConcurrentHashMap<>();
    private ConnectionFactory connectionFactory;

    @EventListener
    public void onSessionClose(SessionClosedEvent event) {
        log.debug("{} onSessionClose: {}", this.getClass().getSimpleName(), event.getUserId());
        if (USER_CHANNEL.containsKey(event.getUserId())) {
            Channel channel = USER_CHANNEL.get(event.getUserId());
            channel.close();
        }
    }

    @EventListener
    public void onSessionInactive(SessionInactiveEvent event) {
        if (event.getUserId() == null) {
            return;
        }

        this.doInactive(event.getUserId());
    }

    @Override
    public void writeAndFlush(NetSession session, BaseMessage message) throws InterruptedException {
        log.debug("write message {}", message.commandId());
        Channel channel = this.getChannel(session);
        if (channel == null) {
            log.warn("no channel, user {} message will discard {}", session.userId(), message.commandId());
            return;
        }
        if (channel.isWritable()) {
            channel.writeAndFlush(message);
        } else {
            log.info("channel {} is not writable!, message will discard", channel.id().asShortText());
        }
    }

    private Channel getChannel(NetSession session) throws InterruptedException {
        Integer key = session.userId();

        if (USER_CHANNEL.containsKey(key)) {
            Channel channel = USER_CHANNEL.get(key);
            if (channel.isActive()) {
                return channel;
            }
        }

        return createChannel(session);
    }

    private Channel createChannel(NetSession session) throws InterruptedException {
        ChannelFuture future = this.connectionFactory.acquire();
        if (future == null) {
            return null;
        }
        Channel channel = future.channel();
        channel.attr(NetSession.sessionKey()).set(session);
        channel.attr(NetSession.userIdKey()).set(session.userId());
        USER_CHANNEL.put(session.userId(), channel);
        // 确保新建的连接成功
        future.sync();
        return channel;
    }

    @Override
    public void inactive(ChannelHandlerContext ctx) {
        log.info("channel inactive {}", ctx.channel().id().asShortText());
        if (!ctx.channel().hasAttr(NetSession.userIdKey())) {
            return;
        }
        Attribute<Integer> attr = ctx.channel().attr(NetSession.userIdKey());
        Integer userId = attr.get();
        this.doInactive(userId);
    }

    @Override
    public void shutdown() {
        log.info("now shutdown forward connections ...");
        for (Integer integer : USER_CHANNEL.keySet()) {
            USER_CHANNEL.get(integer).close();
        }
    }

    private void doInactive(Integer userId) {
        Channel channel = USER_CHANNEL.remove(userId);
        if (channel == null) {
            return;
        }
        log.debug("upstream channel {} receive main channel inactive : {}", channel.id().asShortText(), userId);
        // todo: send message to upstream
        // finally, release channel
        this.connectionFactory.release(channel);
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
}
