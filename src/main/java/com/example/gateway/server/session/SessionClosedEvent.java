package com.example.gateway.server.session;

import org.springframework.context.ApplicationEvent;

public class SessionClosedEvent extends ApplicationEvent {
    private final Integer userId;

    public SessionClosedEvent(Object source, Integer userId) {
        super(source);
        this.userId = userId;
    }

    public Integer getUserId() {
        return this.userId;
    }
}
