package com.sparta.i_mu.dto.responseDto;

import com.sparta.i_mu.entity.Category;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostListResponseDto {
    private Long postId;
    private String postTitle;
    private Long category;
    private LocalDateTime createdAt;
    private int wishlistCount;
    private String content;
    private List<SongResponseDto> songs;
    private int viewCount;

}
