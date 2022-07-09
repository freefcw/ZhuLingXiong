package com.example.gateway.upstream;

import com.example.gateway.module.ModuleType;
import com.example.gateway.server.session.SessionManager;
import com.example.gateway.upstream.common.ConnectionFactory;
import com.example.gateway.upstream.common.ConnectionManager;
import com.example.gateway.upstream.common.PoolConnectionFactory;
import com.example.gateway.upstream.common.PooledChannelHandler;
import com.example.gateway.upstream.forward.ForwardChannelInitializer;
import com.example.gateway.upstream.forward.ForwardConnectionManager;
import com.example.gateway.upstream.proxy.ProxyConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConnectionManagerFactory {
    private final SessionManager sessionManager;

    public ConnectionManagerFactory(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public ConnectionManager buildConnectionManager(ModuleType type, String ip, Integer port) {
        if (type == ModuleType.FORWARD) {
            return this.buildForwardConnectionManager(ip, port);
        }
        return this.buildProxyConnectionManager(ip, port);
    }

    public ConnectionManager buildProxyConnectionManager(String ip, Integer port) {
        ProxyConnectionManager connectionManager = new ProxyConnectionManager();

        PoolConnectionFactory connectionFactory = new PoolConnectionFactory();
        connectionFactory.setRemoteInfo(ip, port);
        connectionFactory.configureInitializer(new PooledChannelHandler(connectionManager, this.sessionManager), 10);

        connectionManager.setConnectionFactory(connectionFactory);

        return connectionManager;
    }

    public ConnectionManager buildForwardConnectionManager(String ip, Integer port) {
        ForwardConnectionManager connectionManager = new ForwardConnectionManager();
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setRemoteInfo(ip, port);
        connectionFactory.configureInitializer(new ForwardChannelInitializer(connectionManager));

        connectionManager.setConnectionFactory(connectionFactory);

        return connectionManager;
    }
}
