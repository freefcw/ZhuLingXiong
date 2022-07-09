package com.example.gateway.module;

import com.example.gateway.server.logic.GatewayModule;

public class ModuleHelper {
    public static boolean conflict(GatewayModule module1, GatewayModule module2) {
        ModuleProperty property1 = module1.getProperty();
        ModuleProperty property2 = module2.getProperty();
        boolean compatible = property1.getCommandEnd() < property2.getCommandStart();
        if (property1.getCommandStart() > property2.getCommandEnd()) {
            compatible = true;
        }
        return !compatible;
    }
}
