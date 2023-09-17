package com.sparta.i_mu.domain.user.dto;

import com.sparta.i_mu.domain.post.dto.PostListResponseDto;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class GetPostResponseDto {
    private String nickname;
    private Page<PostListResponseDto> postList;

    public GetPostResponseDto(String nickname, Page<PostListResponseDto> postResponseDtoList) {
        this.nickname = nickname;
        this.postList = postResponseDtoList;
    }
}
