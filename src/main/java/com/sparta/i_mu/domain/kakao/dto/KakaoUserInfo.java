package com.sparta.i_mu.domain.kakao.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoUserInfo {
    private Long kakaoId;
    private String nickname;
    private String email;
    private String userImage;
}
