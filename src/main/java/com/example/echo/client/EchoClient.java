package com.example.echo.client;

import com.example.gateway.service.auth.AuthInfo;
import com.example.gateway.service.auth.JwtService;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDateTime;

import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class EchoClient {
    private final ConnectionFactory connectionFactory;
    private final JwtService jwtService;

    public EchoClient() {
        this.connectionFactory = new ConnectionFactory();
        this.jwtService = new JwtService();
        this.jwtService.setKey("HsTK2Y4fdIx3ZM9xEOX4Nc0rDePvZHxM");
    }

    public static void main(String[] args) throws InterruptedException {
        EchoClient client = new EchoClient();
        String host = "127.0.0.1";
        Integer port = 7769;
        client.run(host, port);
    }

    public void run(String host, Integer port) throws InterruptedException {
        ChannelFuture future = this.getChannelFuture(host, port);
        EchoService echoService = new EchoService(future);
        Integer userId = 24202421;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                echoService.login(userId, this.jwtService.toJwt(new AuthInfo(userId, LocalDateTime.now().plusDays(3).toDate())));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        synchronized (future.channel()) {
            future.channel().wait();
            executor.shutdown();
        }

        if (!echoService.isLoggedIn()) {
            log.error("login failed!");
            this.connectionFactory.close();
            return;
        } else {
            log.info("login success, continue...");
        }

        echoService.sendHello(userId);

        while (true) {
            Scanner scanner = new Scanner(System.in);
            String next = scanner.nextLine();
            if (Objects.equals(next, "exit")) {
                return;
            }
            echoService.send(next);
        }
    }

    private ChannelFuture getChannelFuture(String host, Integer port) {
        ChannelFuture future = this.connectionFactory.create(host, port);
        future.addListener(future1 -> {
            if (future1.isSuccess()) {
                log.info("connect success");
            } else {
                log.error("connect failed!");
            }
        });
        future.channel().closeFuture().addListener(future1 -> log.info("closing future"));

        return future;
    }

}
