package com.sparta.i_mu.dto.responseDto;

import lombok.Getter;

@Getter
public class LoginResponseDto {
    private String nickname;
    private String userImage;

    public LoginResponseDto(String nickname, String userImage) {
        this.nickname = nickname;
        this.userImage = userImage;
    }
}
