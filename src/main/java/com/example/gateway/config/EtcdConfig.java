package com.example.gateway.config;

import io.etcd.jetcd.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
@Slf4j
public class EtcdConfig {
    private String[] endpoints;

    @Bean(name = "etcdClient", destroyMethod = "close")
    public Client getClient() {
        log.info("connect to etcd: {}", List.of(this.endpoints));
        return Client.builder()
                .endpoints(this.endpoints)
                .connectTimeout(Duration.ofMillis(1000))
                .build();
    }

    @Value("${etcd.endpoints}")
    public void setEndpoints(String endpoints) {
        this.endpoints = endpoints.split(",");
    }
}
