package com.sparta.i_mu.domain.kakao.dto;

import lombok.Getter;

@Getter
public class KakaoTokenPair {
    private final String accessToken;
    private final String refreshToken;

    public KakaoTokenPair(String newAccessToken, String newRefreshToken) {
        this.accessToken = newAccessToken;
        this.refreshToken = newRefreshToken;
    }
}
