package com.sparta.i_mu.controller;

import com.sparta.i_mu.dto.TokenPair;
import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    // 클라이언트가 명시적으로 새롭게 토큰을 발급받고 싶을 때?
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        //검증을 위한 기존의 토큰
        String accessToken = jwtUtil.getAccessTokenFromRequest(request);
        String refreshToken = jwtUtil.getRefreshTokenFromRequest(request);

        TokenPair tokenPair = authService.refreshTokenIfNeeded(accessToken,refreshToken);
        String newAccessToken = tokenPair.getAccessToken();
        String newRefreshToken = tokenPair.getRefreshToken();

        if (newAccessToken != null) {
            response.setHeader(jwtUtil.HEADER_ACCESS_TOKEN, newAccessToken);
            response.setHeader(jwtUtil.HEADER_REFRESH_TOKEN,newRefreshToken);

            return ResponseEntity.status(HttpStatus.OK).body("Tokens refreshed successfully");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unable to refresh token");
        }
    }
}
