package com.example.echo.client;

import com.example.client.proto.Echo;
import com.example.echo.EchoActionType;
import com.example.gateway.module.gateway.GeneralActionType;
import com.example.gateway.proto.General;
import com.example.gateway.server.message.BaseMessage;
import com.example.gateway.server.message.MessageFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class EchoService implements MessageSender {
    private final ChannelFuture channelFuture;
    private final Channel channel;

    public EchoService(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
        this.channel = channelFuture.channel();
    }

    public void login(Integer uid, String token) throws InterruptedException {
        ChannelFuture connectFuture = this.channelFuture.sync();
        ChannelFuture loginFuture = connectFuture.addListener((ChannelFutureListener) future1 -> {
            if (future1.isSuccess()) {
                BaseMessage message = this.buildLoginMessage(uid, token);
                future1.channel().writeAndFlush(message);
            } else {
                log.warn("{} before login failed: {}", uid, future1.cause().getMessage());
            }
        });
        loginFuture.sync().addListener((ChannelFutureListener) future12 -> log.info("login request done!"));
    }

    public void send(String msg) {
        this.channel.eventLoop().schedule(() -> {
            if (this.channel.hasAttr(SessionKey.USER_ID)) {
                Attribute<Integer> attribute = this.channel.attr(SessionKey.USER_ID);
                String message = msg + " " + attribute.get();
                BaseMessage message2 = makeEchoMessage(message);
                this.send(message2);
            }
        }, 10, TimeUnit.MILLISECONDS);
    }

    public void send(BaseMessage message) {
        this.channel.eventLoop().schedule(() -> {
            if (this.channel.hasAttr(SessionKey.USER_ID)) {
                this.channel.writeAndFlush(message);
            }
        }, 10, TimeUnit.MILLISECONDS);
    }

    private BaseMessage makeEchoMessage(String next) {
        Echo.NewEchoMessage.Builder builder = Echo.NewEchoMessage
                .newBuilder()
                .setTs((int) (System.currentTimeMillis() / 1000))
                .setContent(next);
        BaseMessage message = MessageFactory.make(EchoActionType.NEW_ECHO_REQUEST.id());
        Echo.NewEchoMessage message1 = builder.build();
        message.setPayload(message1);
        return message;
    }

    private BaseMessage buildLoginMessage(Integer userId, String token) {
        General.LoginRequest.Builder builder = General.LoginRequest.newBuilder();
        builder.setUserId(userId);
        builder.setToken(token);

        BaseMessage message = MessageFactory.make(GeneralActionType.Auth_Login.id());
        message.setPayload(builder.build());

        return message;
    }

    public boolean isLoggedIn() {
        Attribute<Integer> userIdAttr = this.channel.attr(SessionKey.USER_ID);
        return userIdAttr.get() != null;
    }

}
