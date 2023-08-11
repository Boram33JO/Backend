package com.sparta.i_mu.dto.responseDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoUserInfo {
    private String nickname;
    private String email;
    private String userImage;
}
