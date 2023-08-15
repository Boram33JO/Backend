package com.sparta.i_mu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_KEY = "REFRESH_TOKEN_";

    public void storeRefreshToken(String email, String refreshToken) {
        redisTemplate.opsForValue().set(REFRESH_TOKEN_KEY + email, refreshToken);
    }

    public String getRefreshToken(String email) {
        return (String) redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY + email);
    }

    public void removeRefreshToken(String email) {
        redisTemplate.delete(REFRESH_TOKEN_KEY + email);
    }
}