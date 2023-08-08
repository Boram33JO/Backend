package com.sparta.i_mu.dto.responseDto;

import com.sparta.i_mu.entity.Follow;
import lombok.Getter;

@Getter
public class FollowListResponseDto {
    private Long id;
    private String nickname;
    private String userImage;
    private String introduce;

    public FollowListResponseDto(Follow follow) {
       this.id = follow.getFollowUser().getId();
       this.nickname = follow.getFollowUser().getNickname();
       this.userImage = follow.getFollowUser().getUserImage();
       this.introduce = follow.getFollowUser().getIntroduce();
    }
}
