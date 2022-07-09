package com.example.gateway.server.handler;

import com.example.gateway.component.blacklist.BlackListManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;


@Component
@Slf4j
@ChannelHandler.Sharable
public class BlackListHandler extends ChannelInboundHandlerAdapter {
    private final BlackListManager blackListManager;

    public BlackListHandler(BlackListManager blackListManager) {
        this.blackListManager = blackListManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("remote address: {}", ctx.channel().remoteAddress());
        if (ctx.channel().remoteAddress() instanceof InetSocketAddress remoteAddress) {
            InetAddress inetAddress = remoteAddress.getAddress();
            if (this.blackListManager.isBlocked(inetAddress.getHostAddress())) {
                log.info("ip is blocked: {}", inetAddress.getHostAddress());
                ctx.channel().disconnect();
                ctx.channel().close();
                return;
            }
        }
        super.channelActive(ctx);
    }
}
