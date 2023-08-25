package com.sparta.i_mu.controller;

import com.sparta.i_mu.global.errorCode.ErrorCode;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Token", description = "인증 API Document")
@Slf4j
public class TokenController {

    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    // 클라이언트가 명시적으로 새롭게 토큰을 발급받고 싶을 때?
    @PostMapping("/token/refresh")
    @Operation(summary = "토큰 재발급", description = "토큰 재발급")
    public ResponseResource<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            String accessToken = tokenService.refreshAccessToken(request);
            log.info("accessToken : {}",accessToken);
            response.setHeader(jwtUtil.HEADER_ACCESS_TOKEN, accessToken);
            return ResponseResource.message("AccessToken 재발급이 완료되었습니다.", HttpStatus.OK);

        } catch (Exception e) {
            return ResponseResource.error(ErrorCode.REFRESH_TOKEN_INVALID.getMessage(),ErrorCode.REFRESH_TOKEN_INVALID.getErrorCode());
        }
    }
}
