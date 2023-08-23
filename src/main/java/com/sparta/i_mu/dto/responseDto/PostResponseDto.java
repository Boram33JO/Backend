package com.sparta.i_mu.dto.responseDto;

import com.sparta.i_mu.entity.Location;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostResponseDto {
    private Long userId;
    private Long postId;
    private String nickname;
    private String postTitle;
    private int viewCount;
    private String userImage;
    private String content;
    private Long category;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private LocalDateTime deletedAt;
    private Boolean deleted;
    private Boolean wishlist;
    private Boolean follow;
    private int wishlistCount;
    private List<CommentResponseDto> comments;
    private List<SongResponseDto> songs;
    private Location location;
}
