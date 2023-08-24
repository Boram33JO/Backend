package com.sparta.i_mu.dto.responseDto;

import lombok.Getter;

@Getter
public class LoginResponseDto {
    private String nickname;
    private String email;
    private String userImage;
    private Long userId;
    private String introduce;

    public LoginResponseDto(String nickname, String email, String userImage, String introduce, Long userId) {
        this.nickname = nickname;
        this.email = email;
        this.userImage = userImage;
        this.userId = userId;
        this.introduce = introduce;
    }
}
