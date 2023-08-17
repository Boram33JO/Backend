package com.sparta.i_mu.dto;

import com.sparta.i_mu.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoResult {
    private String accessToken;
    private String refreshToken;
    private KakaoUserResponseDto userInfo;
}
