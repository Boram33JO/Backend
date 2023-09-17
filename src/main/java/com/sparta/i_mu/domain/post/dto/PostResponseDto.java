package com.sparta.i_mu.domain.post.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.sparta.i_mu.domain.comment.dto.CommentResponseDto;
import com.sparta.i_mu.domain.song.dto.SongResponseDto;
import com.sparta.i_mu.domain.location.entity.Location;
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

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime modifiedAt;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime deletedAt;

    private Boolean deleted;
    private Boolean wishlist;
    private Boolean follow;
    private int wishlistCount;
    private List<CommentResponseDto> comments;
    private List<SongResponseDto> songs;
    private Location location;
}
