package com.example.gateway.server.cocurrent;

import com.example.gateway.server.logic.Dispatcher;
import com.example.gateway.server.logic.GatewayModule;
import com.example.gateway.server.message.BaseMessage;
import com.example.gateway.server.session.NetSession;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActionExecutor implements Executor {
    protected NetSession session;
    protected BaseMessage message;

    public ActionExecutor(NetSession session, BaseMessage message) {
        this.session = session;
        this.message = message;
    }

    @Override
    public void onExecute(Dispatcher dispatcher) {
        if (null == message) {
            throw new NullPointerException("message");
        }
        GatewayModule action = dispatcher.getActionHandler(this.message.commandId());
        if (action == null) {
            // unknown action
            log.warn("action ({}) handler not found!", this.message.commandId());
            return;
        }
        try {
            action.handle(this.session, this.message);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() {
        this.session = null;
        this.message = null;
    }
}
