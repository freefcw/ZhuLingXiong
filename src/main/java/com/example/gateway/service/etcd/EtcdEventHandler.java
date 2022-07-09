package com.example.gateway.service.etcd;

public interface EtcdEventHandler {
    void onUpdate(String key, String value);

    void onDelete(String key);
}
