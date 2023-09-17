package com.sparta.i_mu.domain.follow.dto;

import com.sparta.i_mu.domain.follow.entity.Follow;
import lombok.Getter;

@Getter
public class FollowResponseDto {

    private Long id;

    private String email;

    private String nickname;

    public FollowResponseDto(Follow follow) {
        this.id = follow.getFollowUser().getId();
        this.email = follow.getFollowUser().getEmail();
        this.nickname = follow.getFollowUser().getNickname();
    }

}
