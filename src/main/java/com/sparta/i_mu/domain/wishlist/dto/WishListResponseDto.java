package com.sparta.i_mu.domain.wishlist.dto;

import com.sparta.i_mu.domain.song.dto.SongResponseDto;
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
    private Long category;
    private int wishlistCount;
    private int viewCount;

    private List<SongResponseDto> songs;

}
