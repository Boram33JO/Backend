package com.sparta.i_mu.dto.responseDto;

import com.sparta.i_mu.entity.Post;
import lombok.Getter;

@Getter
public class PostListResponseDto {
    private Long id;

    public PostListResponseDto(Post post) {
        this.id = post.getId();
    }
}
