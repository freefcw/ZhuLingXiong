package com.example.gateway.base;

public class GatewayException extends RuntimeException {
    public GatewayException() {
    }

    public GatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
