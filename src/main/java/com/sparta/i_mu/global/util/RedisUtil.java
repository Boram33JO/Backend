package com.sparta.i_mu.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisUtil {
    private static final String REFRESH_TOKEN_KEY = "REFRESH_TOKEN_";
    private static final String BLACK_LIST = "BLACK_LIST";
    private static final String SEARCH_SONG_KEY = "SEARCH_SONG_";
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, Object> redisTemplate1;

    // @RequiredArgContructor를 @Qualifier 과 함께 사용이 불가능하다
    // -> Lombok이 생성하는 생성자에 @Qualifier가 포함되지 않기 때문에
    public RedisUtil(@Qualifier("redisTemplate") RedisTemplate<String, Object> redisTemplate,
                     @Qualifier("redisTemplate1") RedisTemplate<String, Object> redisTemplate1)  {
        this.redisTemplate = redisTemplate;
        this.redisTemplate1 = redisTemplate1;
    }

    public void storeRefreshToken(String accessToken, String refreshToken) {
        redisTemplate.opsForValue().set(REFRESH_TOKEN_KEY + accessToken, refreshToken);
    }
    public void storeBlacklist(String accessToken, long expirationInMinutes) {
        redisTemplate.opsForValue().set(BLACK_LIST + accessToken, String.valueOf(expirationInMinutes));
    }

    public String getRefreshToken(String accessToken) {
        return (String) redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY + accessToken);
    }

    public void removeRefreshToken(String accessToken) {
        redisTemplate.delete(REFRESH_TOKEN_KEY + accessToken);
    }

    public void storeSearchedSong(String accessToken, String refreshToken) {
        redisTemplate1.opsForValue().set(SEARCH_SONG_KEY + accessToken, refreshToken);
    }

    public String getSearchedSong(String keyword) {
        return (String) redisTemplate1.opsForValue().get(SEARCH_SONG_KEY + keyword);
    }

    public void removeSearchedSong(String keyword) {
        redisTemplate1.delete(SEARCH_SONG_KEY + keyword);
    }


}