package com.sparta.i_mu.domain.follow.dto;

import com.sparta.i_mu.domain.user.entity.User;
import lombok.Getter;

@Getter
public class FollowPopularResponseDto {
    private Long id;

    private String nickname;

    private String userImage;

    private Long followCount;

    public FollowPopularResponseDto(User user, Long followCount) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.userImage = user.getUserImage();
        this.followCount = followCount;
    }
}
