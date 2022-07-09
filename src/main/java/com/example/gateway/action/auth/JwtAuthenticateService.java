package com.example.gateway.action.auth;

import com.example.gateway.service.auth.AuthInfo;
import com.example.gateway.service.auth.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtAuthenticateService implements AuthenticateService {
    private final JwtService jwtService;

    public JwtAuthenticateService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void authenticate(Integer userId, String token) {
        try {
            AuthInfo authInfo = this.jwtService.fromJwt(token);
            this.detectExpired(authInfo);
            if (!userId.equals(authInfo.getUserId())) {
                log.warn("userId({}) is not equal token {}", userId, authInfo.getUserId());
                throw new AuthenticationFailed();
            }
        } catch (JwtException jwtException) {
            log.warn("auth failed: {}", jwtException.getMessage());
            throw new AuthenticationFailed();
        }
    }

    private void detectExpired(AuthInfo authInfo) {
        if (!authInfo.isCredentialsNonExpired()) {
            log.warn("auth info expired!");
            throw new AuthenticationFailed();
        }
    }
}
