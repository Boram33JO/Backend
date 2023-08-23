package com.sparta.i_mu.dto.responseDto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class GetPostResponseDto {
    private String nickname;
    private Page<PostListResponseDto> postList;

    public GetPostResponseDto(String nickname, Page<PostListResponseDto> postResponseDtoList) {
        this.nickname = nickname;
        this.postList = postResponseDtoList;
    }
}
