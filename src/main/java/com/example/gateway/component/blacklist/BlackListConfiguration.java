package com.example.gateway.component.blacklist;

import com.example.gateway.service.etcd.ConfigurationMonitor;
import com.example.gateway.service.etcd.EtcdEventHandler;
import com.google.common.net.InetAddresses;
import io.etcd.jetcd.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class BlackListConfiguration implements EtcdEventHandler {
    public static final String MODULES_KEY = "/gateway/blacklist";
    private final ConfigurationMonitor configurationMonitor;
    private final BlackListManager blackListManager;

    public BlackListConfiguration(Client client, BlackListManager blackListManager) {
        this.configurationMonitor = new ConfigurationMonitor(client, MODULES_KEY, this);
        this.blackListManager = blackListManager;
    }

    @PostConstruct
    public void watch() throws ExecutionException, InterruptedException {
        this.configurationMonitor.init();
        this.configurationMonitor.watch();
    }

    public String parseIp(String key) {
        String[] segments = key.split("/"); // ["", "gateway", "modules", "xxx"]
        if (segments.length > 3) {
            return segments[3];
        }
        return "";
    }

    @Override
    public void onUpdate(String key, String value) {
        String ip = this.parseIp(key);
        if (this.isValidIP(ip)) {
            this.blackListManager.blockIp(ip);
        }
    }

    private boolean isValidIP(String ip) {
        return InetAddresses.isInetAddress(ip);
    }

    @Override
    public void onDelete(String key) {
        String ip = this.parseIp(key);
        if (this.isValidIP(ip)) {
            this.blackListManager.unblock(ip);
        }
    }
}
