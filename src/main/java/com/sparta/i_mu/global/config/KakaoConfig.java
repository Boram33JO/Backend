package com.sparta.i_mu.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class KakaoConfig {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${front.url}")
    private String frontUrl;

    @Value("${kakao.client-secret}")
    private String clientSecret;

}
