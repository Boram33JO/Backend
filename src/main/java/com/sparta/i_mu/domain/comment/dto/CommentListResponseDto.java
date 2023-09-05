package com.sparta.i_mu.domain.comment.dto;

import com.sparta.i_mu.domain.comment.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentListResponseDto {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private Long postId;
    private String postTitle;

    public CommentListResponseDto(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.postId = comment.getPost().getId();
        this.postTitle = comment.getPost().getPostTitle();

    }
}
