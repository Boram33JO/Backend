package com.sparta.i_mu.dto.responseDto;

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
