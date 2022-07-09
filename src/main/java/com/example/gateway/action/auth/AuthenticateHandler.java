package com.example.gateway.action.auth;

import com.example.gateway.action.GatewayHandler;
import com.example.gateway.module.gateway.GeneralActionType;
import com.example.gateway.proto.General;
import com.example.gateway.server.message.BaseMessage;
import com.example.gateway.server.message.MessageFactory;
import com.example.gateway.server.session.NetSession;
import com.example.gateway.server.session.SessionManager;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthenticateHandler implements GatewayHandler {
    private final AuthenticateService authenticateService;
    private final SessionManager sessionManager;

    public AuthenticateHandler(AuthenticateService authenticateService, SessionManager sessionManager) {
        this.authenticateService = authenticateService;
        this.sessionManager = sessionManager;
    }

    @Override
    public Boolean support(BaseMessage message) {
        return message.commandId() == GeneralActionType.Auth_Login.id();
    }

    @Override
    public void handle(NetSession session, BaseMessage message) throws InvalidProtocolBufferException {
        General.LoginRequest authLogin = General.LoginRequest.parseFrom(message.payload().nioBuffer());
        log.debug("user login handle {} {}", authLogin.getUserId(), authLogin.getToken());
        try {
            this.authenticateService.authenticate(authLogin.getUserId(), authLogin.getToken());
            log.info("user {} auth success!", authLogin.getUserId());
            this.sessionManager.bindUser(authLogin.getUserId(), session);
            this.sendAuthResponse(session, AuthResult.SUCCESS, authLogin.getUserId());
        } catch (AuthenticationFailed authenticationFailed) {
            log.warn("user {} login failed! {}", authLogin.getUserId(), authLogin.getToken());
            this.sendAuthResponse(session, AuthResult.FAILED, authLogin.getUserId());
        }
    }

    private void sendAuthResponse(NetSession session, AuthResult result, Integer uid) {
        General.LoginResponse.Builder builder = General.LoginResponse.newBuilder();
        builder.setResult(result.number());
        builder.setUserId(uid);
        BaseMessage message = MessageFactory.make(GeneralActionType.Auth_Result.id());
        message.setPayload(builder.build());

        session.writeAndFlush(message);
    }
}
