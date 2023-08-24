package com.sparta.i_mu.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduledTasks {

    private final RedisUtil redisUtil;

    public void checkIdUsers(){

        String hashName = redisUtil.USER_LAST_REQUEST_TIME;
        Long currentTime = System.currentTimeMillis();

        Map<Object, Object> userLastRequestTime = redisUtil.getLastRequestTime();
    }
}
