package com.example.gateway.server.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

@Slf4j
public class SessionInactiveEvent extends ApplicationEvent {
    private final Integer userId;

    public SessionInactiveEvent(Object source, Integer userId) {
        super(source);
        this.userId = userId;
        log.debug("session inactive event! {}", userId);
    }

    public Integer getUserId() {
        return this.userId;
    }
}
