package com.sparta.i_mu.domain.kakao.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoResultResponseDto {
    private String accessToken;
    private String refreshToken;
    private KakaoUserResponseDto userInfoResponse;
}
