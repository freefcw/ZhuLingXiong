package com.example.echo.server;

import com.example.gateway.server.handler.internal.MessageDecoder;
import com.example.gateway.server.handler.internal.MessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class EchoServer {

    private static final int BIZ_GROUP_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final int BIZ_THREAD_SIZE = 8;

    private int port;
    private String host;
    private Channel serverChannel;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public static void main(String[] args) throws InterruptedException {
        EchoServer server = new EchoServer();
        server.setHost("0.0.0.0");
        server.setPort(9933);
        server.start();
    }

    public void start() throws InterruptedException {
        this.bossGroup = new NioEventLoopGroup(BIZ_GROUP_SIZE);
        this.workerGroup = new NioEventLoopGroup(BIZ_THREAD_SIZE);
        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(this.bossGroup, this.workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(this.buildInitializer());

            ChannelFuture future = b.bind(host, port).sync();
            this.serverChannel = future.channel();
            log.info("start echo server at port {}", this.port);

            future.channel().closeFuture().sync();
        } finally {
            this.shutdown();
        }
    }

    private ChannelHandler buildInitializer() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                ChannelPipeline pipeline = socketChannel.pipeline();

                pipeline.addLast(new MessageDecoder());
                pipeline.addLast(new MessageEncoder());
                pipeline.addLast(new EchoHandler());
            }
        };
    }

    private void shutdown() {
        if (this.serverChannel == null) {
            return;
        }
        this.serverChannel.close();
        this.workerGroup.shutdownGracefully();
        this.bossGroup.shutdownGracefully();

        this.serverChannel = null;
        this.workerGroup = null;
        this.bossGroup = null;
    }

    @Value("${netty.server.port:9922}")
    public void setPort(int port) {
        this.port = port;
    }

    @Value("${netty.server.ip:0.0.0.0}")
    public void setHost(String host) {
        this.host = host;
    }
}
