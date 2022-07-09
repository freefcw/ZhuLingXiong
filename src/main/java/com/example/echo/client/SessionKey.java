package com.example.echo.client;

import io.netty.util.AttributeKey;

public class SessionKey {
    public static final AttributeKey<Integer> USER_ID = AttributeKey.newInstance("user_id");
}
