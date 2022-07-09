package com.example.gateway.upstream.proxy;


import com.example.gateway.server.message.BaseMessage;
import com.example.gateway.server.message.MessageConverter;
import com.example.gateway.server.session.NetSession;
import com.example.gateway.upstream.common.ConnectionManager;
import com.example.gateway.upstream.common.PoolConnectionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProxyConnectionManager implements ConnectionManager {
    private PoolConnectionFactory poolConnectionFactory;

    public void setConnectionFactory(PoolConnectionFactory poolConnectionFactory) {
        this.poolConnectionFactory = poolConnectionFactory;
    }

    @Override
    public void writeAndFlush(NetSession session, BaseMessage message) throws InterruptedException {
        try {
            Future<Channel> future = this.poolConnectionFactory.acquire();
            future.addListener(future1 -> {
                Channel channel = (Channel) future1.get();
                channel.writeAndFlush(MessageConverter.toInternalMessage(message, session.userId()));
                this.poolConnectionFactory.release(channel);
            });
        } catch (Exception e) {
            log.error("catch exception on write to remote: {}", e.getMessage());
            log.warn("user {} message will discard {}", session.userId(), message.commandId());
        }
    }

    public void inactive(ChannelHandlerContext ctx) {
        log.debug("inactive channel {}", ctx.channel().id().asShortText());
    }

    @Override
    public void shutdown() {
        log.info("now shutdown proxy connections ...");
        this.poolConnectionFactory.shutdown();
    }
}
