package com.example.echo.client;

import com.example.gateway.service.auth.AuthInfo;
import com.example.gateway.service.auth.JwtService;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDateTime;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class EchoClient {
    private final ConnectionFactory connectionFactory;
    private final JwtService jwtService;
    private final Integer port;
    private final String host;
    private final ExecutorService executor;

    public EchoClient(String host, Integer port, String authKey) {
        this.host = host;
        this.port = port;
        this.connectionFactory = new ConnectionFactory();
        this.jwtService = new JwtService(authKey);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void run(Integer userId, InputSource inputSource) throws InterruptedException {
        ChannelFuture future = this.connectionFactory.create(host, port);

        EchoService echoService = new EchoService(future);

        this.executor.execute(() -> {
            try {
                echoService.login(userId, this.buildToken(userId));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // wait for auth response
        synchronized (future.channel()) {
            future.channel().wait();
        }

        if (!echoService.isLoggedIn()) {
            log.error("login failed!");
            this.connectionFactory.close();
            return;
        } else {
            log.info("login success, continue...");
        }

        echoService.send("message from user " + userId);
        inputSource.handle(echoService);
    }

    private String buildToken(Integer userId) {
        return this.jwtService.toJwt(new AuthInfo(userId, LocalDateTime.now().plusDays(3).toDate()));
    }

    public void close() {
        if (this.executor != null) {
            this.executor.shutdown();
        }
    }
}
