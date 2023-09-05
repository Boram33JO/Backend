package com.sparta.i_mu.domain.user.dto;

import com.sparta.i_mu.domain.follow.dto.FollowListResponseDto;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class GetFollowResponseDto {
    private String nickname;
    private Page<FollowListResponseDto> followList;

    public GetFollowResponseDto(String nickname, Page<FollowListResponseDto> followResponseDtoList) {
        this.nickname = nickname;
        this.followList = followResponseDtoList;
    }
}
