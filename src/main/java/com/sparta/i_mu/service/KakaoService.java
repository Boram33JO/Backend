package com.sparta.i_mu.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.i_mu.dto.KakaoUserInfo;
import com.sparta.i_mu.entity.User;
import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Slf4j(topic = "KAKAO Login")
@Service
@RequiredArgsConstructor
public class KakaoService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${front.url}")
    private String frontUrl;

    @Value("${kakao.client-secret}")
    private String clientSecret;


    public String kakaoLogin(String code) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getAccessToken(code);

        // 2. 토큰으로 카카오 API 호출
        KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(accessToken);

        // 3. 카카오ID로 회원가입 처리
        User kakaoUser = registerKakaoUserIfNeed(kakaoUserInfo);

//        // 4. 강제 로그인 처리
//        Authentication authentication = forceLogin(kakaoUser);
//
//        // 5. response Header에 JWT 토큰 추가
//        kakaoUsersAuthorizationInput(authentication, response);
//
        //4.JWT 토큰 반환
        String createToken = jwtUtil.createAccessToken(kakaoUser.getEmail());

        return createToken;
    }


    public String getAccessToken(String code) throws JsonProcessingException  {
        log.info("인가 code : {} ", code);
        // 1. "인가 코드"로 "액세스 토큰" 요청
        // 요펑 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com")
                .path("/oauth/token")
                .encode()
                .build()
                .toUri();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code"); //카카오 공식문서 기준 authorization_code 로 고정
        body.add("client_id", clientId); // 카카오 Dev 앱 REST API 키
        body.add("redirect_uri", frontUrl); // 카카오 Dev redirect uri
        body.add("code", code); // 프론트에서 인가 코드 요청시 받은 인가 코드값
        body.add("client_secret", clientSecret); // 카카오 Dev 카카오 로그인 Client Secret


        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(body);

        log.info("HTTP 요청");
        log.info("redirect_uri used 전 : {}", frontUrl);
        // HTTP 요청 보내기;
        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );
        log.info("redirect_uri used 후 : {}", frontUrl);
        log.info("토큰 파싱");

        try {
            // HTTP 응답 (JSON) -> 액세스 토큰 파싱
            log.info("HTTP 응답 내용: {}", response.getBody());
            JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
            return jsonNode.get("access_token").asText();

        } catch (JsonProcessingException e) {
            log.error("액세스 토큰 파싱 중 오류 발생: {}", e.getMessage());
            throw e; // 혹은 적절한 예외를 던지거나 다른 처리를 수행합니다.
        }
    }



    public KakaoUserInfo getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        log.info("accessToken : {} ", accessToken);
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com")
                .path("/v2/user/me")
                .encode()
                .build()
                .toUri();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");


        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(new LinkedMultiValueMap<>());

        // HTTP 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );
        log.info("API Response: " + response.getBody());


        // responseBody에 있는 정보를 꺼냄
        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        long id = jsonNode.get("id").asLong();
        String email = jsonNode.get("kakao_account").get("email").asText();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();
        String userImage = jsonNode.get("properties").get("profile_image").asText(); // 또는 "thumbnail_image" 사용

        log.info("카카오 사용자 정보: " + id + ", " + nickname + ", " + email);

        return KakaoUserInfo.builder()
                .email(email)
                .userImage(userImage)
                .nickname(nickname)
                .build();
    }


    public User registerKakaoUserIfNeed(KakaoUserInfo kakaoUserInfo) {
        // DB 에 중복된 email이 있는지 확인

        Long kakaoId = kakaoUserInfo.getId();

        User kakaoUser = userRepository.findByKakaoId(kakaoId).orElse(null);

        if (kakaoUser == null) {
            // 카카오 사용자 email 동일한 email 가진 회원이 있는지 확인
            String kakaoEmail = kakaoUserInfo.getEmail();
            User sameEmailUser = userRepository.findByEmail(kakaoEmail).orElse(null);
            if (sameEmailUser != null) {
                kakaoUser = sameEmailUser;
                // 기존 회원정보에 카카오 Id 추가
                kakaoUser = kakaoUser.kakaoIdUpdate(kakaoId);

            } else {
                // 회원가입
                // password: random UUID
                String password = UUID.randomUUID().toString();
                String encodedPassword = passwordEncoder.encode(password);
                String profile = (kakaoUserInfo.getUserImage() != null && !kakaoUserInfo.getUserImage().isEmpty())
                        ? kakaoUserInfo.getUserImage()
                        : null;

                kakaoUser = User.builder()
                        .email(kakaoEmail)
                        .nickname(kakaoUserInfo.getNickname())
                        .userImage(profile)
                        .password(encodedPassword)
                        .build();
            }
            userRepository.save(kakaoUser);

        }
        return kakaoUser;
    }
//
//    // 4. 강제 로그인 처리
//    private Authentication forceLogin(User kakaoUser) {
//        UserDetails userDetails = new UserDetailsImpl(kakaoUser);
//        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        return authentication;
//    }
//    // 5. response Header에 JWT 토큰 추가
//    private void kakaoUsersAuthorizationInput(Authentication authentication, HttpServletResponse response) {
//        // response header에 token 추가
//        UserDetailsImpl userDetailsImpl = ((UserDetailsImpl) authentication.getPrincipal());
//        String userEmail = userDetailsImpl.getUsername();
//        String token = jwtUtil.createAccessToken(userEmail);
//        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, token);
//    }


}