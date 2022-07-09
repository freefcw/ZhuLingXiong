package com.example.gateway.action.auth;

public enum AuthResult {
    SUCCESS(0),
    FAILED(1),
    ;

    private final int number;

    AuthResult(int i) {
        this.number = i;
    }

    public int number() {
        return number;
    }
}
