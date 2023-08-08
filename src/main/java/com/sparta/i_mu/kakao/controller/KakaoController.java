package com.sparta.i_mu.kakao.controller;


import com.sparta.i_mu.kakao.entity.Kakao;
import com.sparta.i_mu.kakao.entity.oauth.OauthToken;
import com.sparta.i_mu.kakao.jwt.JwtProperties;
import com.sparta.i_mu.kakao.service.KakaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoService kakaoService;

    @GetMapping("/kakao")
    public ResponseEntity<Void> getKakao(HttpServletRequest request) {
        String redirectUri = "http://" + request.getHeader("host") + "/api/oauth/token";
        URI uri = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com")
                .path("/oauth/authorize")
                .queryParam("client_id", kakaoService.clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();
        log.info("uri = " + uri.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uri);
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);

    }

    // 프론트에서 인가코드 받아오는 url
    @GetMapping("/token")
    public ResponseEntity getLogin(@RequestParam("code") String code, HttpServletRequest request) { //(1)

        String redirectUri = "http://" + request.getHeader("host") + "/api/oauth/token";

        // 넘어온 인가 코드를 통해 access_token 발급
        OauthToken oauthToken = kakaoService.getAccessToken(code, redirectUri);

        //(2)
        // 발급 받은 accessToken 으로 카카오 회원 정보 DB 저장 후 JWT 를 생성
        String jwtToken = kakaoService.saveUserAndGetToken(oauthToken.getAccess_token());

        //(3)
        HttpHeaders headers = new HttpHeaders();
        headers.add(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken);

        //(4)
        return ResponseEntity.ok().headers(headers).body("success");
    }


    @GetMapping("/me")
    public ResponseEntity<Object> getCurrentUser(HttpServletRequest request) {
        Kakao kakao = kakaoService.getUser(request);
        return ResponseEntity.ok().body(kakao);
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        // 서비스 내에서 로그아웃 처리를 진행합니다.
        kakaoService.logout(request, response);

        // 로그아웃 완료 후, 다시 로그인 페이지로 리다이렉트합니다.
        String loginRedirectUrl = "http://" + request.getHeader("host") + "/api/oauth/kakao";
        return ResponseEntity.status(HttpStatus.FOUND).header("Location", loginRedirectUrl).build();
    }
}