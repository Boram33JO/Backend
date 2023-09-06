package com.sparta.i_mu.domain.notification.dto;

import com.sparta.i_mu.domain.notification.entity.Notification;
import com.sparta.i_mu.global.util.NotificationType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class NotificationResponse {

    private Long id;

    private String content;

    private String url;

    private LocalDateTime createdAt;

    private boolean read;

    private NotificationType notificationType;

    @Builder
    public NotificationResponse(Long id, String content, String url, LocalDateTime createdAt, boolean read, NotificationType notificationType) {
        this.id = id;
        this.content = content;
        this.url = url;
        this.createdAt = createdAt;
        this.read = read;
        this.notificationType = notificationType;
    }

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .content(notification.getContent())
                .url(notification.getUrl())
                .createdAt(notification.getCreatedAt())
                .read(notification.isRead())
                .notificationType(notification.getNotificationType())
                .build();
    }
}
