package com.sparta.i_mu.dto;

import lombok.Getter;

@Getter
public class TokenPair {
    private final String accessToken;
    private final String refreshToken;

    public TokenPair(String newAccessToken, String newRefreshToken) {
        this.accessToken = newAccessToken;
        this.refreshToken = newRefreshToken;
    }
}
