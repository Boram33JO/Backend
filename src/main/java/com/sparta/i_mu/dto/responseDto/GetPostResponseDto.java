package com.sparta.i_mu.dto.responseDto;

import lombok.Getter;

import java.util.List;

@Getter
public class GetPostResponseDto {
    private String nickname;
    private List<PostListResponseDto> postList;

    public GetPostResponseDto(String nickname, List<PostListResponseDto> postResponseDtoList) {
        this.nickname = nickname;
        this.postList = postResponseDtoList;
    }
}
