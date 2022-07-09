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
import java.util.concurrent.ThreadPoolExecutor;

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
        client.run("127.0.0.1", 7769);
    }

    public void run(String host, Integer port) throws InterruptedException {
        ChannelFuture future = getChannelFuture(host, port);
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
            log.info("enter wait...");
            future.channel().wait();
            log.info("wait break!");
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
        // test to bytes
//            BaseMessage message1 = makeChatMessage(next);
//            log.info("bytes {}", message1.getContentBytes());
//            log.info("byteBuff {}", message1.getContentBytes().asReadOnlyByteBuffer());
//
//            String encodingStr = Base64.getEncoder().encodeToString(message1.toByteArray());
//            log.info("base64 {}", encodingStr);

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
        return future;
    }

}
