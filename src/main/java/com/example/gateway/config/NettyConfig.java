package com.example.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "netty")
@Configuration
@Data
public class NettyConfig {
    private static final int BIZ_GROUP_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final int BIZ_THREAD_SIZE = 8;


    private String ip;

    private int port;

    private int bossCount;

    private int workerCount;

    private boolean keepAlive;

    private int backlog;

    /**
     * if server is linux, use EpollEventLoopGroup instead of.
     */
    private boolean useLinuxEpoll;

    /**
     * open buffer check
     */
    private boolean checkBufferLack;
}
