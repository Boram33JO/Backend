package com.sparta.i_mu.domain.kakao.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.i_mu.domain.kakao.dto.KakaoResultResponseDto;
import com.sparta.i_mu.domain.kakao.dto.KakaoUserResponseDto;
import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.domain.kakao.service.KakaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "KakaoUser", description = "유저 API Document")
public class KakaoController {

    private final JwtUtil jwtUtil;
    private final KakaoService kakaoService;

    // 카카오 로그인
    @PostMapping("/oauth/token")
    @Operation(summary = "카카오 로그인", description = "카카오 로그인")
    public ResponseEntity<KakaoUserResponseDto> kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        KakaoResultResponseDto kakaoResultResponseDto = kakaoService.kakaoLogin(code);
        HttpHeaders headers = new HttpHeaders();
        headers.add(jwtUtil.HEADER_ACCESS_TOKEN,  kakaoResultResponseDto.getAccessToken()); // accessToken 토큰을 헤더에 추가
        headers.add(jwtUtil.HEADER_REFRESH_TOKEN,  kakaoResultResponseDto.getRefreshToken()); // refreshToken 토큰을 헤더에 추가
        return new ResponseEntity<>(kakaoResultResponseDto.getUserInfoResponse(), headers, HttpStatus.OK);
    }

}
