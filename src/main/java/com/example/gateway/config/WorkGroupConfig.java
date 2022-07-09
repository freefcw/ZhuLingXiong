package com.example.gateway.config;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkGroupConfig {
    private final NettyConfig nettyConfig;

    public WorkGroupConfig(NettyConfig nettyConfig) {
        this.nettyConfig = nettyConfig;
    }

    @Bean(name = "bossGroup", destroyMethod = "shutdownGracefully")
    public EventLoopGroup bossGroup() {
        int bossCount = this.nettyConfig.getBossCount();
        if (this.nettyConfig.isUseLinuxEpoll() && Epoll.isAvailable()) {
            return new EpollEventLoopGroup(bossCount);
        } else {
            return new NioEventLoopGroup(bossCount);
        }
    }

    @Bean(name = "workerGroup", destroyMethod = "shutdownGracefully")
    public EventLoopGroup workerGroup() {
        int workerCount = this.nettyConfig.getWorkerCount();
        if (this.nettyConfig.isUseLinuxEpoll() && Epoll.isAvailable()) {
            return new EpollEventLoopGroup(workerCount);
        } else {
            return new NioEventLoopGroup(workerCount);
        }
    }
}
