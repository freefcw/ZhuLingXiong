package com.example.gateway.module;

import com.example.gateway.service.etcd.ConfigurationMonitor;
import com.example.gateway.service.etcd.EtcdEventHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.etcd.jetcd.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class ModuleConfiguration implements EtcdEventHandler {
    public static final String MODULES_KEY = "/gateway/modules";
    private final ConfigurationMonitor configurationMonitor;
    private final ModuleManager moduleManager;
    private final ObjectMapper objectMapper;

    public ModuleConfiguration(Client client, ModuleManager moduleManager, ObjectMapper objectMapper) {
        this.moduleManager = moduleManager;
        this.objectMapper = objectMapper;
        this.configurationMonitor = new ConfigurationMonitor(client, MODULES_KEY, this);
    }

    @PostConstruct
    public void watch() throws ExecutionException, InterruptedException {
        this.configurationMonitor.init();
        this.configurationMonitor.watch();
    }


    private ModuleProperty toModuleProperty(String key, String value) {
        try {
            ModuleProperty property = this.objectMapper.readValue(value, new TypeReference<>() {
            });
            String name = this.getModuleName(key);
            property.setName(name);
            return property;
        } catch (JsonProcessingException e) {
            log.error("parse module {} property failed: {}", key, value);
        }
        return null;
    }

    public String getModuleName(String key) {
        if (!this.isModuleKey(key)) {
            return "";
        }
        String[] segments = key.split("/"); // ["", "gateway", "modules", "xxx"]
        if (segments.length > 3) {
            return segments[3];
        }
        return "";
    }

    public boolean isModuleKey(String key) {
        return key.startsWith(MODULES_KEY) && !key.equals(MODULES_KEY);
    }

    @Override
    public void onUpdate(String key, String value) {
        ModuleProperty moduleProperty = this.toModuleProperty(key, value);
        if (moduleProperty != null) {
            this.moduleManager.registerModule(moduleProperty);
        }
    }

    @Override
    public void onDelete(String key) {
        String moduleName = this.getModuleName(key);
        if (moduleName.length() > 0) {
            this.moduleManager.deregisterModule(key);
        }
    }
}
