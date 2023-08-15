package com.sparta.i_mu.dto.responseDto;

import com.sparta.i_mu.entity.Wishlist;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class WishListResponseDto {
    private Long postId;

    private String postTitle;

    private LocalDateTime createdAt;

    private String content;

    private Long userId;

    private String nickname;

    private String userImage;

    private List<PostListSongResponseDto> songs;

}
