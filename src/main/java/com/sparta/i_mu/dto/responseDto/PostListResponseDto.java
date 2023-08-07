package com.sparta.i_mu.dto.responseDto;

import com.sparta.i_mu.entity.Post;
import com.sparta.i_mu.entity.PostSongLink;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
public class PostListResponseDto {
    private Long id;
    private List<SongResponseDto> songs;

    public PostListResponseDto(Post post) {
        this.id = post.getId();
//        this.songs = post.getPostSongLink().stream().map(SongResponseDto::new).toList();
    }
}
