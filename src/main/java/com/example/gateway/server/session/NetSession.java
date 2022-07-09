package com.example.gateway.server.session;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NetSession implements Session {
    private final boolean reliable; //  is reliable pipe

    private final boolean retry;    //  retry switch

    private final int maxRetryCount; //  max retry times

    private volatile Channel channel;

    private final UUID sessionId;
    private Integer userId;

    public NetSession(Channel channel) {
        this(channel, false, false, 5);
    }

    public NetSession(Channel channel, boolean reliable, boolean retry, int maxRetryCount) {
        this.sessionId = UUID.randomUUID();
        this.channel = channel;
        this.reliable = reliable;
        this.retry = retry;
        this.maxRetryCount = maxRetryCount;
        this.channel.attr(sessionKey()).set(this);
    }

    public static AttributeKey<NetSession> sessionKey() {
        return AttributeKey.valueOf("session");
    }

    public static AttributeKey<Integer> userIdKey() {
        return AttributeKey.valueOf("userId");
    }

    @Override
    public UUID id() {
        return this.sessionId;
    }

    @Override
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public Integer userId() {
        return this.userId;
    }

    @Override
    public Channel channel() {
        return this.channel;
    }

    @Override
    public boolean isActive() {
        return this.channel != null && this.channel.isActive();
    }

    public void transport(Object data, ChannelFutureListener listener) {
        transport(data, listener, 0);
    }

    /**
     * @param data       The transport message data.
     * @param listener   the callback listener.
     * @param retryCount retry transport data count, default is zero.
     */
    public void transport(Object data, ChannelFutureListener listener, final int retryCount) {
        if (null == channel) {
            log.debug("ChannelHandlerContext is null.");
            return;
        }
        if (!channel.isActive()) {
            log.debug("Channel is null or inactive. Channel id:{}", channel.id().toString());
            return;
        }
        if (reliable || channel.isWritable()) {
            if (listener == null) {
                channel.writeAndFlush(data, channel.voidPromise());
            } else {
                channel.writeAndFlush(data).addListener(listener);
            }
        } else {
            if (retryCount > 0) {
                //  if retry count exceed max retry count, discard message
                boolean shouldRetry = this.retry && retryCount < this.maxRetryCount;
                if (!shouldRetry) {
                    log.warn("retry exceed max times");
                    return;
                }
            }
            channel.eventLoop().schedule(() -> transport(data, listener, retryCount + 1), 1L, TimeUnit.SECONDS);
        }
    }

    @Override
    public void writeAndFlush(Object msg) {
        this.writeAndFlush(msg, null);
    }

    @Override
    public void writeAndFlush(Object message, ChannelFutureListener listener) {
        if (null == channel || !channel.isActive()) {
            return;
        }
        if (channel.isWritable()) {
            if (listener == null) {
                channel.writeAndFlush(message, channel.voidPromise());
            } else {
                channel.writeAndFlush(message).addListener(listener);
            }
        } else {
            channel.eventLoop().schedule(() -> writeAndFlush(message, listener), 1L, TimeUnit.SECONDS);
        }
    }

    @Override
    public void close() {
        if (channel != null) {
            if (channel.isActive()) {
                channel.close();
            }
            channel = null;
        }
    }

    @Override
    public boolean isAuthenticated() {
        return this.userId != null;
    }
}
