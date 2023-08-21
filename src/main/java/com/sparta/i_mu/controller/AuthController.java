package com.sparta.i_mu.controller;

import com.sparta.i_mu.dto.TokenPair;
import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Auth", description = "인증 API Document")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    // 클라이언트가 명시적으로 새롭게 토큰을 발급받고 싶을 때?
    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "토큰 재발급")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {

        try {
            TokenPair tokenPair = authService.refreshTokenIfNeeded(request);

            response.setHeader(jwtUtil.HEADER_ACCESS_TOKEN, tokenPair.getAccessToken());
            response.setHeader(jwtUtil.HEADER_REFRESH_TOKEN, tokenPair.getRefreshToken());

            return ResponseEntity.status(HttpStatus.OK).body("Tokens refreshed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unable to refresh token");
        }
    }
}
