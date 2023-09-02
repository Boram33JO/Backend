package com.sparta.i_mu.dto.responseDto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
@Getter
public class TopPostResponseDto {
    private final List<PostResponseDto> wishlistTopPosts;
    private final List<PostResponseDto> viewCountTopPosts;

    public TopPostResponseDto(List<PostResponseDto> wishlistTopPosts, List<PostResponseDto> viewCountTopPosts) {
        this.wishlistTopPosts = wishlistTopPosts;
        this.viewCountTopPosts = viewCountTopPosts;
    }
}
