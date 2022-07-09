package com.example.gateway.server.cocurrent;

public class ConcurrentEvent {

    private Executor executor;

    public Executor getExecutor() {
        return executor;
    }

    public void setValues(Executor executor) {
        this.executor = executor;
    }

    public void clearValues() {
        setValues(null);
    }
}
