package com.example.gateway.server;


import com.example.gateway.config.NettyConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Objects;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class GatewayServer implements ApplicationRunner, ApplicationListener<ContextClosedEvent> {
    private final NettyConfig nettyConfig;
    private final ServerBootstrap bootstrap;
    private Channel serverChannel;

    public GatewayServer(NettyConfig nettyConfig, ServerBootstrap bootstrap) {
        this.nettyConfig = nettyConfig;

        this.bootstrap = bootstrap;
    }

    public void start() throws InterruptedException {
        log.info("Starting Server @ {}:{}...", this.nettyConfig.getIp(), this.nettyConfig.getPort());

        ChannelFuture future = this.bootstrap.bind(this.nettyConfig.getIp(), this.nettyConfig.getPort()).sync();
        this.serverChannel = future.channel();
        future.addListener(future1 -> {
            if (future1.isCancelled()) {
                log.info("Server stopped!: {}", future1.cause().toString());
            } else if (!future1.isSuccess()) {
                log.info("Server stop failed: {}", future1.cause().toString());
            } else {
                log.info("Server started {}:{}", this.nettyConfig.getIp(),
                        this.nettyConfig.getPort());
            }
        });

        future.channel().closeFuture().sync();
    }

    @Override
    public void run(ApplicationArguments args) {
        Executors.newSingleThreadExecutor().execute(new Thread(() -> {
            try {
                start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }

    @Override
    public void onApplicationEvent(@NonNull ContextClosedEvent event) {
        log.warn("Receive application close event! {}", event);
        if (this.serverChannel != null) {
            this.shutdown();
        }
        log.info("Server stopped!");
    }


    private void shutdown() {
        if (this.serverChannel == null) {
            return;
        }
        this.serverChannel.close().syncUninterruptibly();
        if (!Objects.isNull(this.serverChannel.parent())) {
            this.serverChannel.parent().close().syncUninterruptibly();
        }
    }

    @PreDestroy
    public void destroy() {
        this.shutdown();
    }
}