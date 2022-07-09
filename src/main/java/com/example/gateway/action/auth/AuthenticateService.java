package com.example.gateway.action.auth;

public interface AuthenticateService {
    void authenticate(Integer userId, String token);
}
