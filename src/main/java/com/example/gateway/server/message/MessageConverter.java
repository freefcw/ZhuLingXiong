package com.example.gateway.server.message;

import com.example.gateway.server.message.internal.InternalMessage;

public class MessageConverter {
    public static InternalMessage toInternalMessage(BaseMessage message, Integer userId) {
        InternalMessage internalMessage = MessageFactory.make(message.commandId(), userId);
        internalMessage.setPayload(message.payload());

        return internalMessage;
    }

    public static BaseMessage toBaseMessage(InternalMessage message) {
        BaseMessage baseMessage = MessageFactory.make(message.commandId());
        baseMessage.setPayload(message.payload());

        return baseMessage;
    }
}
