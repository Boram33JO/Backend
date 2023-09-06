package com.sparta.i_mu.domain.token.service;

import com.sparta.i_mu.global.errorCode.ErrorCode;
import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.global.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;

    /**
     * 액세스 토큰 만료 후 refresh 토큰으로 재발급 요청 시
     * accessToken 재발급
     *
     * @return
     */
    public String refreshAccessToken(HttpServletRequest request) {

        String accessToken = jwtUtil.BEARER + jwtUtil.getAccessTokenFromRequest(request);
        String refreshToken = jwtUtil.getRefreshTokenFromRequest(request);

        // 1. 먼저 refreshToken의 유효성을 검사합니다.
        if (refreshToken != null && !jwtUtil.validateAccessToken(refreshToken)) {
            log.info("RefreshToken 이 유효하지 않습니다.");
            redisUtil.removeRefreshToken(accessToken); // 필요한 경우 Redis에서 토큰 삭제
            throw new IllegalArgumentException(ErrorCode.REFRESH_TOKEN_INVALID.getMessage());
        }

        // 2. refreshToken이 만료되지 않았을 때
        String email = jwtUtil.getUserInfoFromToken(refreshToken).getSubject();
        String refreshTokenRedis = redisUtil.getRefreshToken(accessToken);

        log.info("accessToken : {}", accessToken);
        log.info("refreshToken : {}", refreshToken);
        log.info("email : {}", email);
        log.info("refreshTokenRedis : {}", refreshTokenRedis);

        if (refreshToken.equals(jwtUtil.substringToken(refreshTokenRedis))) {
            // refreshToken이 유효하다면 새로운 accessToken 발급
            String newAccessToken = jwtUtil.createAccessToken(email);
            log.info("newAccessToken : {}", newAccessToken);
            redisUtil.removeRefreshToken(accessToken);
            redisUtil.storeRefreshToken(newAccessToken, refreshTokenRedis);
            return newAccessToken;
            // Redis에 새로운 리프레시 토큰 저장
        } else {
            log.info("REDIS의 REFRESH_TOKEN과 일치하지 않습니다.");
            throw new IllegalArgumentException(ErrorCode.REFRESH_TOKEN_MISMATCH.getMessage());
        }
    }

}
