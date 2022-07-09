package com.example.gateway.action.heartbeat;

import org.springframework.context.ApplicationEvent;

public class HeartBeatEvent extends ApplicationEvent {
    private final Integer userId;

    public HeartBeatEvent(Object source, Integer userId) {
        super(source);
        this.userId = userId;
    }

    public Integer getUserId() {
        return this.userId;
    }
}
