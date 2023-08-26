package com.sparta.i_mu.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final String REFRESH_TOKEN_KEY = "REFRESH_TOKEN_";
    private final String SEARCH_SONG_KEY = "SEARCH_SONG_";
    private final String SEARCH_KEYWORD_KEY = "SEARCH_KEYWORD_";
    private final String BLACKLIST_KEY= "BLACKLIST_KEY_";
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
        redisTemplate.opsForZSet().incrementScore(SEARCH_KEYWORD_KEY, keyword , 1);
        redisTemplate.expire("SEARCH_KEYWORD_",1,TimeUnit.DAYS);
    }
    public Set<String> getSearchKeyword() {
       return redisTemplate.opsForZSet().reverseRange("SEARCH_KEYWORD_", 0,9);
    }


    //블랙 리스트 등록
    public void storeBlacklist(String userInfo, String accessToken, Long expirationInSeconds){
        redisTemplate.opsForValue().set(BLACKLIST_KEY + userInfo,accessToken,expirationInSeconds,TimeUnit.SECONDS);
    }

    //블랙 리스트 조회
    public String isBlacklisted(String email) {
       return redisTemplate.opsForValue().get(BLACKLIST_KEY + email);
    }
  
  
    //이메일 인증 관련
    public void setDataExpire(String key, String value, long duration) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }
    
    public void removeData(String email) {
        redisTemplate.delete(email);
    }
    
    public String getData(String email) {
        return (String) redisTemplate.opsForValue().get(email);
    }

    //조회수 ip  관련
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

    public void setDataExpir(String confirmNum, String getTo, long duration) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(confirmNum, getTo, expireDuration);
    }

    public void setPostViewList(String postViewKey, Long postId) {
        String key = "POST_VIEW_" + postViewKey;
        String value = String.valueOf(postId);

        long todayEndSecond = LocalDate.now().atTime(LocalTime.MAX).toEpochSecond(ZoneOffset.UTC);
        long currentSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        log.info("조회수 남은 시간 : {}", todayEndSecond - currentSecond);

        redisTemplate.opsForList().rightPushAll(key, value);
        redisTemplate.expire(key, todayEndSecond - currentSecond, TimeUnit.SECONDS);
    }

    public List<String> getPostViewList(String postViewKey) {
        String key = "POST_VIEW_" + postViewKey;
        return redisTemplate.opsForList().range(key, 0, -1);
    }

}

