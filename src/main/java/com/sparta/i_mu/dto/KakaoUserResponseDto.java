package com.sparta.i_mu.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoUserResponseDto {
    private Long userId;
    private String email;
    private String nickname;
    private String userImage;
    private String introduce;
    private Long kakaoId;

}
