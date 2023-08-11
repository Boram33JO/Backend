package com.sparta.i_mu.dto.responseDto;

import com.sparta.i_mu.entity.User;
import lombok.Getter;

@Getter
public class FollowPopularResponseDto {
    private Long id;

    private String nickname;

    private String userImage;

    public FollowPopularResponseDto(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.userImage = user.getUserImage();
    }
}
