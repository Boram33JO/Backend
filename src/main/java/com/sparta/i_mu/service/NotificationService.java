package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.responseDto.NotificationResponse;
import com.sparta.i_mu.dto.responseDto.NotificationsResponse;
import com.sparta.i_mu.entity.Notification;
import com.sparta.i_mu.entity.User;
import com.sparta.i_mu.global.util.NotificationType;
import com.sparta.i_mu.repository.EmitterRepository;
import com.sparta.i_mu.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;

    public SseEmitter connect(Long userId, String lastEventId, HttpServletResponse response) {
        String id = userId + "_" + System.currentTimeMillis();
        log.info(id);
        SseEmitter emitter = emitterRepository.save(id, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> emitterRepository.deleteById(id));
        emitter.onTimeout(() -> emitterRepository.deleteById(id));
        emitter.onError((e) -> emitterRepository.deleteById(id));

        // 503 에러 방지, 더미 전송
        sendToClient(emitter, id, "연결되었습니다. userId: " + userId);
        // NGINX PROXY 에서의 필요설정, 불필요한 버퍼링방지
        response.setHeader("X-Accel-Buffering", "no");

        // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방
        if (!lastEventId.isEmpty()) {
            Map<String, Object> events = emitterRepository.findAllEventCacheStartWithId(String.valueOf(userId));
            events.entrySet().stream()
                    .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                    .forEach(entry -> sendToClient(emitter, entry.getKey(), entry.getValue()));
        }

        return emitter;
    }

    private void sendToClient(SseEmitter emitter, String id, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(id)
                    .name("sse")
                    .data(data));
        } catch (IOException exception) {
            emitterRepository.deleteById(id);
//            throw new RuntimeException("SSE 연결 오류");
        }
    }

    @Transactional
    public void send(User receiver, NotificationType notificationType, String content, String url) {
        Notification notification = createNotification(receiver, notificationType, content, url);
        String id = String.valueOf(receiver.getId());
        log.info("알림 받을 사람 ID: {}", id);
        log.info("알림 받을 내용: {}", content);
        log.info("알림 받을 url: {}", url);
        notificationRepository.save(notification);
        Map<String, SseEmitter> sseEmitters = emitterRepository.findAllStartWithById(id);
        sseEmitters.forEach(
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, notification);
                    sendToClient(emitter, key, NotificationResponse.from(notification));
                }
        );
    }

    private Notification createNotification(User receiver, NotificationType notificationType, String content, String url) {
        return Notification.builder()
                .receiver(receiver)
                .content(content)
                .notificationType(notificationType)
                .url(url)
                .isRead(false)
                .build();
    }

    @Transactional
    public NotificationsResponse findAllById(Long userId) {
        List<NotificationResponse> responses = notificationRepository.findAllByReceiverId(userId).stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
        long unreadCount = responses.stream()
                .filter(notification -> !notification.isRead())
                .count();

        return NotificationsResponse.of(responses, unreadCount);
    }

    @Transactional
    public void readNotification(Long userId, Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 알림입니다."));

        if (!notification.getReceiver().getId().equals(userId)) {
            throw new IllegalArgumentException("로그인한 유저가 아닙니다.");
        }

        notification.read();
    }

}
