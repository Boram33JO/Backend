package com.sparta.i_mu.domain.post.dto;

import com.sparta.i_mu.domain.post.dto.PostResponseDto;
import lombok.Getter;

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
