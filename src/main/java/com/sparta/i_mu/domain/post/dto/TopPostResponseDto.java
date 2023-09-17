package com.sparta.i_mu.domain.post.dto;

import com.sparta.i_mu.domain.post.dto.PostResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Getter
@NoArgsConstructor
public class TopPostResponseDto {
    private List<PostResponseDto> wishlistTopPosts;
    private List<PostResponseDto> viewCountTopPosts;

    public TopPostResponseDto(List<PostResponseDto> wishlistTopPosts, List<PostResponseDto> viewCountTopPosts) {
        this.wishlistTopPosts = wishlistTopPosts;
        this.viewCountTopPosts = viewCountTopPosts;
    }
}
