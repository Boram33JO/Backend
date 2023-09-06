package com.sparta.i_mu.domain.notification.dto;

import com.sparta.i_mu.domain.notification.entity.Notification;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class NotificationResponse {

    private Long id;

    private String content;

    private Long postId;

    private String postTitle;

    private Long userId;

    private String nickname;

    private String userImage;

    private LocalDateTime createdAt;

    private boolean read;


    @Builder
    public NotificationResponse(Long id, String content, Long postId,String postTitle, Long userId, String nickname, String userImage, LocalDateTime createdAt, boolean read) {
        this.id = id;
        this.content = content;
        this.postId = postId;
        this.postTitle = postTitle;
        this.userId = userId;
        this.nickname = nickname;
        this.userImage = userImage;
        this.createdAt = createdAt;
        this.read = read;
    }

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .content(notification.getContent())
                .postId(notification.getPostId())
                .postTitle(notification.getPostTitle())
                .userId(notification.getSender().getId())
                .nickname(notification.getSender().getNickname())
                .userImage(notification.getSender().getUserImage())
                .createdAt(notification.getCreatedAt())
                .read(notification.isRead())
                .build();
    }
}
