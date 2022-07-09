package com.example.gateway.manage;

import com.example.gateway.manage.service.StatImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ManageServer implements ApplicationRunner, ApplicationListener<ContextClosedEvent> {
    private final StatImpl stat;
    private Server server;
    private Integer port;

    public ManageServer(StatImpl stat) {
        this.stat = stat;
    }

    public void start() throws IOException, InterruptedException {
        log.info("Starting Manage server @ {}...", this.port);
        this.server = ServerBuilder.forPort(this.port)
                .addService(this.stat)
                .build()
                .start();
        this.blockUtilShutdown();
    }

    @Override
    public void run(ApplicationArguments args) {
        Executors.newSingleThreadExecutor().execute(new Thread(() -> {
            try {
                this.start();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }));
    }

    @PreDestroy
    public void stop() throws InterruptedException {
        System.out.println("*** shutting down Manage server since Application is shutting down");
        if (this.server != null) {
            this.server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    public void blockUtilShutdown() throws InterruptedException {
        if (this.server != null) {
            this.server.awaitTermination();
        }
    }

    @Override
    public void onApplicationEvent(@NonNull ContextClosedEvent event) {
        try {
            this.stop();
        } catch (InterruptedException e) {
            log.warn("catch InterruptedException");
        }
    }

    @Value("${rpc.port:50001}")
    public void setPort(Integer port) {
        this.port = port;
    }
}
