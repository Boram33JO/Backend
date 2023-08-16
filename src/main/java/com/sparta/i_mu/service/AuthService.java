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
        return jwtUtil.validateAccessToken(accessToken);
    }
    //리프레시 토큰이 유효한지 확인하는 메서드
    public boolean isRefreshTokenValid(String refreshToken) {
        return jwtUtil.validateRefreshToken(refreshToken);
    }

    // 액세스 토큰을 refreshToken을 이용해 재발급 받을 때
    public String refreshAccessToken(String refreshToken) {
        String userEmail = jwtUtil.getUserInfoFromToken(refreshToken).getSubject();
        log.info("refreshToken 의 subject 값 User_Email : {} ", userEmail);
        if(jwtUtil.validateRefreshToken(refreshToken)) {
            // refreshToken이 유효하다면 새로운 accessToken 발급
            log.info("refreshToken 검증 : {}", jwtUtil.validateRefreshToken(refreshToken));
            return jwtUtil.createAccessToken(userEmail);
        } else {
            throw new RuntimeException("Invalid refresh token");
        }
    }

    // 클라이언트가 명시적으로 재발급을 원할 때 - 리프레시 토큰도 재발급 이바로 되게
    public TokenPair refreshTokenIfNeeded(String accessToken, String refreshToken) {
        String userEmail = jwtUtil.getUserInfoFromToken(refreshToken).getSubject();
        if(jwtUtil.validateRefreshToken(refreshToken)) {
            // refreshToken이 유효하다면 새로운 accessToken 발급
            String newAccessToken = jwtUtil.createAccessToken(userEmail);
            // 주기적인 리프레시 토큰 갱신 (여기서는 간단하게 매번 새로운 리프레시 토큰을 발급하도록 함)
            String newRefreshToken = jwtUtil.createRefreshToken(userEmail);
            // 이전 refreshToken 삭제
            redisService.removeRefreshToken(accessToken);
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
     * @param accessToken
     * @param response
     */
    public void refreshTokenRegularly(String refreshToken, String accessToken, HttpServletResponse response) {
        log.info("일주일 간격으로 refreshToken 갱신메서드 현재 refreshToken : {}", refreshToken);
        Claims refreshTokenClaims = jwtUtil.getUserInfoFromToken(refreshToken);
        String email = refreshTokenClaims.getSubject();
        Date issuedAt = refreshTokenClaims.getIssuedAt();
        Date date = new Date();
        long daysSinceLastRefresh = (date.getTime() - issuedAt.getTime()) / (1000 * 60 * 60 * 24);
//        long minuteSinceLastRefresh = (date.getTime() - issuedAt.getTime()) / (1000*60);
        log.info("현재 시간 : {}",  date.getTime());
        log.info("리프레시 토큰 등록 시간: {}", issuedAt.getTime());
        log.info("토큰 시간 차이 : {} ", daysSinceLastRefresh);
        // 리프레시 토큰이 일주일 이상된 경우 새로 발급 / 분
        if(daysSinceLastRefresh >= 7) {
            // 이전 refreshToken 삭제
            redisService.removeRefreshToken(accessToken);
            String newRefreshToken = jwtUtil.createRefreshToken(email);
            log.info("new RefreshToken : {} " , newRefreshToken);
            redisService.storeRefreshToken(accessToken, newRefreshToken); // Redis에 새 리프레시 토큰 저장
            response.setHeader(jwtUtil.HEADER_REFRESH_TOKEN, newRefreshToken); // 응답 헤더에 새 리프레시 토큰 설정
        }
        else log.info("기존의 refreshToken 유지");
    }

}
