package com.example.gateway.module;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ModuleType {
    INTERNAL(0),
    PROXY(1),
    FORWARD(2),
    ;

    private final int number;

    ModuleType(int number) {
        this.number = number;
    }

    public boolean is(int i) {
        return this.number == i;
    }

    @JsonValue
    public int number() {
        return this.number;
    }
}
