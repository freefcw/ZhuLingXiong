package com.example.gateway.manage.service;

import com.example.gateway.ManagementProto;
import com.example.gateway.StatGrpc;
import com.example.gateway.Type;
import com.example.gateway.module.ModuleManager;
import com.example.gateway.module.ModuleProperty;
import com.example.gateway.module.ModuleType;
import com.example.gateway.server.session.SessionManager;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class StatImpl extends StatGrpc.StatImplBase {
    private final ModuleManager moduleManager;
    private final SessionManager sessionManager;

    public StatImpl(ModuleManager moduleManager, SessionManager sessionManager) {
        this.moduleManager = moduleManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public void getOnlineUsers(Type.Empty request, StreamObserver<ManagementProto.OnlineUsers> responseObserver) {
        ManagementProto.OnlineUsers onlineUsers = ManagementProto.OnlineUsers.newBuilder()
                .addAllUsers(this.sessionManager.getUsers())
                .build();

        responseObserver.onNext(onlineUsers);
        responseObserver.onCompleted();
    }

    @Override
    public void getStat(Type.Empty request, StreamObserver<ManagementProto.GatewayStat> responseObserver) {
        SessionManager.Stat stat = this.sessionManager.getStatInfo();
        ManagementProto.GatewayStat gatewayStat = ManagementProto.GatewayStat.newBuilder()
                .setTotal(stat.getTotal())
                .build();

        responseObserver.onNext(gatewayStat);
        responseObserver.onCompleted();
    }

    @Override
    public void getModules(Type.Empty request, StreamObserver<ManagementProto.ModuleList> responseObserver) {
        List<ManagementProto.Module> modules = this.moduleManager.getModules()
                .stream().map(module -> {
                    ModuleProperty property = module.getProperty();
                    ManagementProto.Module.Builder moduleBuilder = ManagementProto.Module.newBuilder()
                            .setName(property.getName())
                            .setCommandStart(property.getCommandStart())
                            .setCommandEnd(property.getCommandEnd());

                    ModuleType type = property.getType();
                    if (type != ModuleType.INTERNAL) {
                        moduleBuilder.setIp(property.getIp())
                                .setPort(property.getPort());
                        if (type == ModuleType.FORWARD) {
                            moduleBuilder.setType(com.example.gateway.ManagementProto.ModuleType.Forward);
                        } else {
                            moduleBuilder.setType(com.example.gateway.ManagementProto.ModuleType.Proxy);
                        }

                    } else {
                        moduleBuilder.setType(com.example.gateway.ManagementProto.ModuleType.Internal);
                    }

                    return moduleBuilder.build();
                }).toList();
        ManagementProto.ModuleList moduleList = ManagementProto.ModuleList.newBuilder()
                .addAllModules(modules)
                .build();

        responseObserver.onNext(moduleList);
        responseObserver.onCompleted();
    }
}
