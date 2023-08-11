package com.sparta.i_mu.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoResult {
    private String token;
    private KakaoUserInfo userInfo;
}
