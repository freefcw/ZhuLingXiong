package com.example.gateway.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
@Slf4j
public class JwtService {
    private Key key;

    private Integer periodDays = 3;

    public JwtService() {
    }

    public JwtService(String key) {
        this.setKey(key);
    }

    @Value("${jwt.key:mBWP2sVN9Bmw3mQhAwhF2R6CF7oHzX8G}")
    public void setKey(String key) {
        this.key = Keys.hmacShaKeyFor(key.getBytes());
    }

    @Value("${jwt.period-days:3}")
    public void setPeriodDays(Integer days) {
        this.periodDays = days;
    }

    public AuthInfo fromJwt(String jwt) {
        io.jsonwebtoken.JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(key).setAllowedClockSkewSeconds(Integer.MAX_VALUE).build();
        Claims claims = jwtParser.parseClaimsJws(jwt).getBody();
        // Disable expiration check in JWT, never throw ExpiredJwtException
        // Expiration is controlled by Spring through UserDetails.isCredentialsNonExpired
        // SimpleUserDetails knows expiration time.
        // It's checked in  org.springframework.security.core.userdetails.UserDetailsChecker.check()
        // at org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider.authenticate()
        return new AuthInfo(Integer.parseInt(claims.getId()), claims.getExpiration());
    }

    public String toJwt(AuthInfo authInfo) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.plusDays(this.periodDays);
        return Jwts.builder().setId(authInfo.getUserId().toString())
//                .setSubject(userDetails.getId().toString())
//                .setIssuedAt(now.toDate())
                .setExpiration(expiration.toDate()).signWith(this.key).compact();
    }
}