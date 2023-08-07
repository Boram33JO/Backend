package com.sparta.i_mu.dto.responseDto;

import com.sparta.i_mu.entity.Comment;
import lombok.Getter;

@Getter
public class CommentListResponseDto {
    private Long id;
    private String content;

    public CommentListResponseDto(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();

    }
}
