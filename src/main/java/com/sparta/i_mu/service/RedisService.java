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

    // 최초 발급된 Access을 반환하는 비즈니스 로직(조회?)
    public String getAccessToken(String refreshToken) {
        return redisTemplate.opsForValue().get(refreshToken);
    }

    // 최초 발급된 Access Token과 Refresh Token을 저장 (key : Access, value : refresh)
    public void saveAccessToken(String refreshToken, String accessToken, Date refreshExpire) {
        redisTemplate.opsForValue().set(refreshToken, accessToken); // key : value
        redisTemplate.expireAt(refreshToken, refreshExpire); // 키 값에 해당 객체 만료일(자동삭제) 설정
    }

    // 해당 데이터를 삭제하는 메서드?
    // accessToken이 유효한데? refreshToken에 요청을 할 경우?
    public void deleteToken(String refreshToken) {
        redisTemplate.delete(refreshToken);
    }
}