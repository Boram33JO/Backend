package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.TokenPair;
import com.sparta.i_mu.global.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final RedisService redisService;
    private final JwtUtil jwtUtil;

    // 액세스 토큰이 유효한지 확인하는 메서드
    public boolean isAccessTokenValid(String accessToken) {
        boolean isValid = jwtUtil.validateToken(accessToken);
        if (isValid) {
            log.info("AccessToken 유효성 검증 성공");
        } else {
            log.warn("AccessToken 유효성 검증 실패");
        }
        return isValid;
    }



    // 액세스 토큰을 refreshToken을 이용해 재발급 받을 때
    public String refreshAccessToken(String refreshToken) {
        String userEmail = jwtUtil.getUserInfoFromToken(refreshToken).getSubject();
        log.info("refreshToken 의 key 값 User_Email : {} ", userEmail);
        if(jwtUtil.validateToken(refreshToken)) {
            // refreshToken이 유효하다면 새로운 accessToken 발급
            log.info("refreshToken : {}", jwtUtil.validateToken(refreshToken));
            return jwtUtil.createAccessToken(userEmail);
        } else {
            throw new RuntimeException("Invalid refresh token");
        }
    }

    // 클라이언트가 명시적으로 재발급을 원할 때 - 리프레시 토큰도 재발급 이바로 되게
    public TokenPair refreshTokenIfNeeded(String expiredAccessToken, String refreshToken) {
        String userEmail = jwtUtil.getUserInfoFromToken(expiredAccessToken).getSubject();
        if(jwtUtil.validateToken(refreshToken)) {
            // 이전 refreshToken 삭제
            redisService.removeRefreshToken(userEmail);
            // refreshToken이 유효하다면 새로운 accessToken 발급
            String newAccessToken = jwtUtil.createAccessToken(userEmail);
            // 주기적인 리프레시 토큰 갱신 (여기서는 간단하게 매번 새로운 리프레시 토큰을 발급하도록 함)
            String newRefreshToken = jwtUtil.createRefreshToken(userEmail);
            // Redis에 새로운 리프레시 토큰 저장
            redisService.storeRefreshToken(userEmail, newRefreshToken);

            return new TokenPair(newAccessToken, newRefreshToken);
        } else {
            throw new RuntimeException("Invalid refresh token");
        }
    }

    /**
     * refreshToken을 일주일 주기로 재 업로드 하는 코드
     * @param refreshToken
     * @param userEmail
     * @param response
     */
    public void refreshTokenRegularly(String refreshToken, String userEmail, HttpServletResponse response) {
        log.info("일주일 간격으로 refreshToken 갱신메서드 현재 refreshToken : {}", refreshToken);
        Claims refreshTokenClaims = jwtUtil.getUserInfoFromToken(refreshToken);
        Date issuedAt = refreshTokenClaims.getIssuedAt();
        long daysSinceLastRefresh = (new Date().getTime() - issuedAt.getTime()) / (1000 * 60 * 60 * 24);

        // 리프레시 토큰이 일주일 이상된 경우 새로 발급
        if(daysSinceLastRefresh >= 7) {
            // 이전 refreshToken 삭제
            log.info("새롭게 refreshToken 발급하는 메서드 ");
            redisService.removeRefreshToken(userEmail);
            String newRefreshToken = jwtUtil.createRefreshToken(userEmail);
            redisService.storeRefreshToken(userEmail, newRefreshToken); // Redis에 새 리프레시 토큰 저장
            response.setHeader(jwtUtil.HEADER_REFRESH_TOKEN, newRefreshToken); // 응답 헤더에 새 리프레시 토큰 설정
        }
    }

}
