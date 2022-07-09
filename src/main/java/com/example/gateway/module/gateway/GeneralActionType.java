package com.example.gateway.module.gateway;

public enum GeneralActionType {
    Common_Response(1),
    Auth_Login(10),
    Auth_Result(11),
    Auth_Logout(12),
    HeartBeat(2),
    ;

    private final short id;

    GeneralActionType(Integer id) {
        this.id = id.shortValue();
    }

    public short id() {
        return id;
    }

    public static GeneralActionType fromCommand(short id) {
        for (GeneralActionType value : GeneralActionType.values()) {
            if (value.id == id) {
                return value;
            }
        }

        return null;
    }
}
