package com.example.gateway.service.auth;

import lombok.Data;

import java.util.Date;

@Data
public class AuthInfo {
    private Integer userId;
    private Date expiredAt;

    public AuthInfo(Integer userId, Date expiredAt) {
        this.userId = userId;
        this.expiredAt = expiredAt;
    }

    public boolean isCredentialsNonExpired() {
        Date now = new Date();
        return now.before(this.expiredAt);
    }
}
