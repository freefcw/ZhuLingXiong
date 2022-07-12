package com.example.gateway.module;

import com.example.gateway.module.gateway.InternalModule;
import com.example.gateway.server.logic.GatewayModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class ModuleManager {
    private final CopyOnWriteArrayList<GatewayModule> moduleList;
    private final ModuleBuilder moduleBuilder;
    private InternalModule internalModule;

    public ModuleManager(ModuleBuilder moduleBuilder) {
        this.moduleBuilder = moduleBuilder;
        this.moduleList = new CopyOnWriteArrayList<>();
    }

    @PostConstruct
    public void init() {
        this.registerModule(this.internalModule);
    }

    public boolean registerModule(GatewayModule module) {
        log.info("registerModule module: {}", module.name());
        for (GatewayModule module1 : this.moduleList) {
            if (Objects.equals(module.getProperty().getName(), module1.getProperty().getName())) {
                log.error("module name conflict: {}", module.getProperty().getName());
                return false;
            }
            if (ModuleHelper.conflict(module, module1)) {
                log.error("module conflict: {} -- {}", module, module1);
                return false;
            }
        }
        this.moduleList.add(module);
        return true;
    }

    public boolean registerModule(ModuleProperty property) {
        log.info("registerModule module with property {}", property);
        if (this.hasModule(property.getName())) {
            log.info("module {} is exist, will replace", property.getName());
        }
        this.deregisterModule(property.getName());
        return this.registerModule(this.moduleBuilder.buildWithProperty(property));
    }

    public boolean hasModule(String name) {
        return this.moduleList.stream().anyMatch(gatewayModule -> {
            return gatewayModule.name().equals(name);
        });
    }


    public GatewayModule getModule(short commandId) {
        for (GatewayModule module : this.moduleList) {
            if (module.support(commandId)) {
                return module;
            }
        }
        log.info("no module support command {}", commandId);
        return null;
    }

    @Resource
    public void setGatewayModule(InternalModule internalModule) {
        this.internalModule = internalModule;
    }

    public List<GatewayModule> getModules() {
        return Collections.unmodifiableList(this.moduleList);
    }

    public boolean deregisterModule(String name) {
        for (GatewayModule module : this.moduleList) {
            if (module.name().equals(name)) {
                log.info("deregister module {}", name);
                this.moduleList.remove(module);
                module.shutdown();
                return true;
            }
        }
        log.warn("module {} not found, can not deregister.", name);
        return false;
    }
}
