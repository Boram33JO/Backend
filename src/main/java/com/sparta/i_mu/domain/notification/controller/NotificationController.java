package com.sparta.i_mu.domain.notification.controller;

import com.sparta.i_mu.domain.notification.dto.NotificationsResponse;
import com.sparta.i_mu.global.security.UserDetailsImpl;
import com.sparta.i_mu.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/notifications")
@Tag(name = "Notification", description = "알림 API Document")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "로그인 한 유저 SSE 연결", description = "로그인 한 유저 SSE 연결")
    public SseEmitter connect (@AuthenticationPrincipal UserDetailsImpl userDetails,
                               @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId,
                               HttpServletResponse response) {

        return notificationService.connect(userDetails.getUserId(), lastEventId, response);

    }

    @GetMapping
    @Operation(summary = "로그인 한 유저의 모든 알림 조회", description = "로그인 한 유저의 모든 알림 조회")
    public ResponseEntity<NotificationsResponse> notifications(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        return ResponseEntity.ok().body(notificationService.findAllById(userDetails.getUserId()));

    }

    @PatchMapping("/{id}")
    @Operation(summary = "알림 읽음 상태 변경", description = "알림 읽음 상태 변경")
    public ResponseEntity<Void> readNotification(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                 @PathVariable Long id) {

        notificationService.readNotification(userDetails.getUserId(), id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

}
