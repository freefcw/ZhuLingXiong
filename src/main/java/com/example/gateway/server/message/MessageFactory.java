package com.example.gateway.server.message;

import com.example.gateway.server.message.internal.InternalHeader;
import com.example.gateway.server.message.internal.InternalMessage;

public class MessageFactory {
    public static BaseMessage make(short command) {
        Header header = new Header(command);
        BaseMessage message = new BaseMessage();
        message.setHeader(header);

        return message;
    }

    public static InternalMessage make(short command, Integer userId) {
        InternalHeader header = new InternalHeader(command, userId);
        InternalMessage message = new InternalMessage();
        message.setHeader(header);

        return message;
    }
}
