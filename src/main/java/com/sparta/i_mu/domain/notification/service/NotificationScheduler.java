package com.sparta.i_mu.domain.notification.service;

import com.sparta.i_mu.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationRepository notificationRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupNotification() {

        notificationRepository.deleteByCreatedAtLessThanEqual(LocalDateTime.now().minusDays(7));

    }

}
