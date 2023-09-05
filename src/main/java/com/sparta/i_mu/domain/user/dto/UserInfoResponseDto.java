package com.sparta.i_mu.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponseDto {
    Long userId;
    String nickname;
    String userImage;
    String introduce;
}
