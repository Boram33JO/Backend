package com.sparta.i_mu.dto.responseDto;

import com.sparta.i_mu.entity.Follow;
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
