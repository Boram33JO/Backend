package com.sparta.i_mu.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {
    private static final String REFRESH_TOKEN_KEY = "REFRESH_TOKEN_";
    private static final String SEARCH_SONG_KEY = "SEARCH_SONG_";
    private static final String SEARCH_KEYWORD_ = "SEARCH_KEYWORD_";
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

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

    /**
     * REDIS에 저장기간 한달
     * @param keyword
     * @param serializedSongs
     */
    public void storeSearchedSong(String keyword, String serializedSongs) {
        redisTemplate.opsForValue().set(SEARCH_SONG_KEY + keyword, serializedSongs);
        redisTemplate.expire(SEARCH_SONG_KEY + keyword, 30, TimeUnit.DAYS );
    }

    public String getSearchedSong(String keyword){
        return (String) redisTemplate.opsForHash().get(SEARCH_SONG_KEY , keyword);
    }

    /**
     * 인기검색어 RESET은 하루에 한번
     * @param keyword
     */
    public void storeSearchKeyword(String keyword) {
        redisTemplate.opsForZSet().incrementScore(SEARCH_KEYWORD_, keyword , 1);
        redisTemplate.expire("SEARCH_KEYWORD_",1,TimeUnit.DAYS);
    }
    public Set<String> getSearchKeyword() {
       return redisTemplate.opsForZSet().reverseRange("SEARCH_KEYWORD_", 0,9);
    }

    public String isBlacklisted(String accessToken) {
       return redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY + jwtUtil.BEARER + accessToken);
    }

    public void storeUserIp(String userIp, Long postId) {
        String key = userIp + "_" + postId;
        redisTemplate.opsForValue().set(key, "true");
        redisTemplate.expire(key, 30, TimeUnit.DAYS );
    }

    public Boolean isUserIp(String userIp, Long postId) {
        String key = userIp + "_" + postId;
        if (redisTemplate.hasKey(key)) {
            return true;
        }
        return false;
    }

    public void setUserIpList(String userIp, Long postId) {
        String value = String.valueOf(postId);

        long todayEndSecond = LocalDate.now().atTime(LocalTime.MAX).toEpochSecond(ZoneOffset.UTC);
        long currentSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        log.info("조회수 남은 시간 : {}", todayEndSecond - currentSecond);

        redisTemplate.opsForList().rightPushAll(userIp, value);
        redisTemplate.expire(userIp, todayEndSecond - currentSecond, TimeUnit.SECONDS);
    }

    public List<String> getUserIpList(String userIp) {
        return redisTemplate.opsForList().range(userIp, 0, -1);
    }


}