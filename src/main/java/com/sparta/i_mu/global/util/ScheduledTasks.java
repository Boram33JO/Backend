package com.sparta.i_mu.global.util;

import com.sparta.i_mu.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduledTasks {

    private final RedisUtil redisUtil;
    private final UserService userService;

    @Scheduled(fixedRate = 60000)
    public void checkIdUsers(){

        String hashName = redisUtil.USER_LAST_REQUEST_TIME;
        Long currentTime = System.currentTimeMillis();
        Map<Object, Object> userLastRequestTime = redisUtil.getLastRequestTime();

        for (Map.Entry<Object,Object> entry : userLastRequestTime.entrySet()) {
            String userEmail = (String) entry.getKey();
            Long lastRequestTime = (Long) entry.getValue();

            if(currentTime - lastRequestTime > 30 * 60 * 1000) {
                redisUtil.getLastRequestTime();
//                userService.logout();
            }
        }
    }
}
