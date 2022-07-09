package com.example.gateway.config;

import com.example.gateway.server.handler.ChildInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ResourceLeakDetector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class BoostrapConfig {
    private final NettyConfig nettyConfig;
    private final ChildInitializer childChannelInitializer;
    private final EventLoopGroup boosGroup;
    private final EventLoopGroup workerGroup;

    public BoostrapConfig(NettyConfig nettyConfig,
                          ChildInitializer childChannelInitializer,
                          @Qualifier("bossGroup") EventLoopGroup boosGroup,
                          @Qualifier("workerGroup") EventLoopGroup workerGroup) {
        this.nettyConfig = nettyConfig;
        this.childChannelInitializer = childChannelInitializer;
        this.boosGroup = boosGroup;
        this.workerGroup = workerGroup;
    }

    @Bean(name = "serverBootstrap")
    public ServerBootstrap bootstrap() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(this.boosGroup, this.workerGroup);
        if (this.nettyConfig.isUseLinuxEpoll() && Epoll.isAvailable()) {
            bootstrap.channel(EpollServerSocketChannel.class);
        } else {
            bootstrap.channel(NioServerSocketChannel.class);
        }

        bootstrap.childHandler(this.childChannelInitializer);
        bootstrap.option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000) // default 30000
                .childOption(ChannelOption.SO_KEEPALIVE, this.nettyConfig.isKeepAlive())
                .childOption(ChannelOption.TCP_NODELAY, true);

        if (this.nettyConfig.isCheckBufferLack()) {
            log.warn("enable cache buffer check");
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
        }

        return bootstrap;
    }
}
