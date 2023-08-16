package com.sparta.i_mu.dto.responseDto;

import lombok.Getter;

import java.util.List;

@Getter
public class GetFollowResponseDto {
    private String nickname;
    private List<FollowListResponseDto> followList;

    public GetFollowResponseDto(String nickname, List<FollowListResponseDto> followResponseDtoList) {
        this.nickname = nickname;
        this.followList = followResponseDtoList;
    }
}
