package com.example.gateway.upstream.common;

import com.example.gateway.base.GatewayException;

public class UpstreamUnavailable extends GatewayException {
    public UpstreamUnavailable(String message, Throwable cause) {
        super(message, cause);
    }
}
