package com.sparta.i_mu.dto.responseDto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class GetFollowResponseDto {
    private String nickname;
    private Page<FollowListResponseDto> followList;

    public GetFollowResponseDto(String nickname, Page<FollowListResponseDto> followResponseDtoList) {
        this.nickname = nickname;
        this.followList = followResponseDtoList;
    }
}
