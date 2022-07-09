package com.example.echo;

public enum EchoActionType {
    NEW_ECHO_REQUEST(200),
    ECHO_RESPONSE(201),
    ;

    private final short id;

    EchoActionType(Integer id) {
        this.id = id.shortValue();
    }

    public short id() {
        return id;
    }

    public static EchoActionType fromCommand(short id) {
        for (EchoActionType value : EchoActionType.values()) {
            if (value.id == id) {
                return value;
            }
        }

        return null;
    }
}
