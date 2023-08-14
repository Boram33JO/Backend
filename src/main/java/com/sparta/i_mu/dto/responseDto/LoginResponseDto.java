package com.sparta.i_mu.dto.responseDto;

import lombok.Getter;

@Getter
public class LoginResponseDto {
    private String nickname;
    private String userImage;
    private Long userId;

    public LoginResponseDto(String nickname, String userImage, Long userId) {
        this.nickname = nickname;
        this.userImage = userImage;
        this.userId = userId;
    }
}
