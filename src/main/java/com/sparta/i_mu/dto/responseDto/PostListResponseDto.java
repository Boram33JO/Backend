package com.sparta.i_mu.dto.responseDto;

import com.sparta.i_mu.entity.Location;
import com.sparta.i_mu.entity.Post;
import com.sparta.i_mu.entity.PostSongLink;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostListResponseDto {
    private Long postId;

//    private String postTitle;

    private String content;

    private List<PostListSongResponseDto> songs;

}
