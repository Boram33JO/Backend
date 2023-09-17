package com.sparta.i_mu.domain.comment.dto;

import com.sparta.i_mu.domain.comment.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {
    private Long commentId;

    private String content;

    private String nickname;

    private String userImage;

    private LocalDateTime createdAt;

    private Long userId;

    public CommentResponseDto (Comment comment) {
        this.commentId = comment.getId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.nickname = comment.getUser().getNickname();
        this.userImage = comment.getUser().getUserImage();
        this.userId = comment.getUser().getId();
    }

}
