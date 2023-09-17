package com.sparta.i_mu.domain.follow.dto;

import com.sparta.i_mu.domain.follow.entity.Follow;
import lombok.Getter;

@Getter
public class FollowListResponseDto {
    private Long userId;
    private String nickname;
    private String userImage;
    private String introduce;

    public FollowListResponseDto(Follow follow) {
       this.userId = follow.getFollowUser().getId();
       this.nickname = follow.getFollowUser().getNickname();
       this.userImage = follow.getFollowUser().getUserImage();
       this.introduce = follow.getFollowUser().getIntroduce();
    }
}
