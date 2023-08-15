package com.sparta.i_mu.dto.responseDto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostListResponseDto {
    private Long postId;

    private String postTitle;

    private LocalDateTime createdAt;

    private String content;

    private List<PostListSongResponseDto> songs;

}
