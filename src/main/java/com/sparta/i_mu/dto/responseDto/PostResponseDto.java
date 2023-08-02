package com.sparta.i_mu.dto.responseDto;

import com.sparta.i_mu.entity.Comment;
import com.sparta.i_mu.entity.Location;
import com.sparta.i_mu.entity.Song;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostResponseDto {
    private Long userId;
    private String nickname;
    private String content;
    private String category;
    private LocalDateTime createdAt;
    private Boolean wishlist;
    private Long wishlistCount;
    private List<Comment> comments;
    private List<SongResponseDto> songs;
    private Location location;
}
