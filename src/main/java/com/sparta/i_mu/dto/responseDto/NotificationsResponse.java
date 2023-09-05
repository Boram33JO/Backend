package com.sparta.i_mu.dto.responseDto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class NotificationsResponse {

    private List<NotificationResponse> notificationResponses;

    private long unreadCount;

    @Builder
    public NotificationsResponse(List<NotificationResponse> notificationResponses, long unreadCount) {
        this.notificationResponses = notificationResponses;
        this.unreadCount = unreadCount;
    }

    public static NotificationsResponse of(List<NotificationResponse> notificationResponses, long count) {
        return NotificationsResponse.builder()
                .notificationResponses(notificationResponses)
                .unreadCount(count)
                .build();
    }
}