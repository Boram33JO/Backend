package com.sparta.i_mu.service;

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
public class AuthService {

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
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            log.info("RefreshToken 이 유효하지 않습니다.");
            redisUtil.removeRefreshToken(accessToken); // 필요한 경우 Redis에서 토큰 삭제
            throw new IllegalArgumentException(ErrorCode.REFRESH_TOKEN_INVALID.getMessage());
        }

        String nickname = jwtUtil.getUserInfoFromToken(refreshToken).getSubject();
        String refreshTokenRedis = redisUtil.getRefreshToken(accessToken);

        log.info("accessToken : {}", accessToken);
        log.info("refreshToken : {}", refreshToken);
        log.info("nickname : {}", nickname);
        log.info("refreshTokenRedis : {}", refreshTokenRedis);

        if (refreshToken.equals(jwtUtil.substringToken(refreshTokenRedis))) {
            // refreshToken이 유효하다면 새로운 accessToken 발급
            String newAccessToken = jwtUtil.createAccessToken(nickname);
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
//    /**
//     * refreshToken을 일주일 주기로 재 업로드 하는 코드
//     * @param accessToken
//     * @param response
//     */
//    public void refreshTokenRegularly(String accessToken, HttpServletResponse response) {
//        log.info("일주일 간격으로 refreshToken 갱신");
//        Claims userInfo = jwtUtil.getUserInfoFromToken(accessToken);
//        String nickname = userInfo.getSubject();
//        Date issuedAt = userInfo.getIssuedAt();
//        Date date = new Date();
//        accessToken = jwtUtil.BEARER + accessToken;
////        long daysSinceLastRefresh = (date.getTime() - issuedAt.getTime()) / (1000 * 60 * 60 * 24);
//        long minuteSinceLastRefresh = (date.getTime() - issuedAt.getTime()) / (1000*60);
//        log.info("현재 시간 : {}",  date.getTime());
//        log.info("리프레시 토큰 등록 시간: {}", issuedAt.getTime());
//        log.info("토큰 시간 차이 : {} ", minuteSinceLastRefresh);
//        // 리프레시 토큰이 일주일 이상된 경우 새로 발급 / 분
//        if(minuteSinceLastRefresh >= 1) {
//            // 이전 refreshToken 삭제
//            redisUtil.removeRefreshToken(accessToken);
//
//            String newRefreshToken = jwtUtil.createRefreshToken(nickname);
//            redisUtil.storeRefreshToken(accessToken, newRefreshToken); // Redis에 새 리프레시 토큰 저장
//            response.setHeader(jwtUtil.HEADER_REFRESH_TOKEN, newRefreshToken); // 응답 헤더에 새 리프레시 토큰 설정
//        }
//        else log.info("기존의 refreshToken 유지");
//    }

    }
