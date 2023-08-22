package com.sparta.i_mu.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {
    private static final String REFRESH_TOKEN_KEY = "REFRESH_TOKEN_";
    private static final String SEARCH_SONG_KEY = "SEARCH_SONG_";
    public final RedisTemplate<String, String> redisTemplate;

    //refreshToken 관련 메서드
    public void storeRefreshToken(String accessToken, String refreshToken) {
        redisTemplate.opsForValue().set(REFRESH_TOKEN_KEY + accessToken, refreshToken);
    }

    public String getRefreshToken(String accessToken) {
        return (String) redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY + accessToken);
    }

    public void removeRefreshToken(String accessToken) {
        redisTemplate.delete(REFRESH_TOKEN_KEY + accessToken);
    }

    // 노래 검색 관련 메서드
    public void storeSearchedSong(String keyword, String serializedSongs) {
        redisTemplate.opsForValue().set(SEARCH_SONG_KEY + keyword, serializedSongs);
    }

    public String getSearchedSong(String keyword){
        return (String) redisTemplate.opsForValue().get(SEARCH_SONG_KEY + keyword);
    }

    public void removeSearchedSong(String keyword) {
        redisTemplate.delete(SEARCH_SONG_KEY + keyword);
    }

}