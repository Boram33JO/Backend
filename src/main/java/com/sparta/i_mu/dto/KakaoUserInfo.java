package com.sparta.i_mu.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoUserInfo {
    private Long id;
    private String nickname;
    private String email;
    private String userImage;
}
