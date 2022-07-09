package com.example.gateway.service.etcd;

import com.google.common.base.Charsets;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class ConfigurationMonitor {
    private final Client etcdClient;
    private final String monitorKey;
    private final EtcdEventHandler etcdEventHandler;

    public ConfigurationMonitor(Client etcdClient, String monitorKey, EtcdEventHandler etcdEventHandler) {
        this.etcdClient = etcdClient;
        this.monitorKey = monitorKey;
        this.etcdEventHandler = etcdEventHandler;
    }

    public void init() throws ExecutionException, InterruptedException {
        GetOption option = GetOption.newBuilder()
                .isPrefix(true)
                .build();
        GetResponse getResponse = this.etcdClient.getKVClient()
                .get(ByteSequence.from(this.monitorKey, UTF_8), option)
                .get();

        if (getResponse.getKvs().isEmpty()) {
            log.error("no keys found!");
            return;
        }

        for (KeyValue kv : getResponse.getKvs()) {
            String key = Optional.ofNullable(kv.getKey()).map(bs -> bs.toString(Charsets.UTF_8)).orElse("");
            String value = Optional.ofNullable(kv.getValue()).map(bs -> bs.toString(Charsets.UTF_8)).orElse("");
            log.info("handle module registry: {}", key);
            this.etcdEventHandler.onUpdate(key, value);
        }
    }

    public void watch() {
        Watch.Watcher watcher = null;
        WatchOption watchOptions = WatchOption.newBuilder()
                .isPrefix(true)
                .build();
        log.info("watch etcd module change events: key={}", this.monitorKey);
        try {
            ByteSequence watchKey = ByteSequence.from(this.monitorKey, UTF_8);

            watcher = this.etcdClient.getWatchClient().watch(watchKey, watchOptions, response -> {
                for (WatchEvent event : response.getEvents()) {
                    WatchEvent.EventType eventType = event.getEventType();
                    KeyValue keyValue = event.getKeyValue();

                    String key = Optional.ofNullable(keyValue.getKey()).map(bs -> bs.toString(Charsets.UTF_8)).orElse("");
                    String value = Optional.ofNullable(keyValue.getValue()).map(bs -> bs.toString(Charsets.UTF_8)).orElse("");
                    log.info("type={}, key={}, value={}", eventType.toString(), key, value);

                    if (event.getEventType() == WatchEvent.EventType.PUT) {
                        this.etcdEventHandler.onUpdate(key, value);
                    }
                    if (event.getEventType() == WatchEvent.EventType.DELETE) {
                        this.etcdEventHandler.onDelete(key);
                    }
                }
            });
        } catch (Exception e) {
            if (watcher != null) {
                watcher.close();
            }
            throw e;
        }
    }
}
