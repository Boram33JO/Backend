package com.sparta.i_mu.domain.kakao.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.i_mu.global.config.KakaoConfig;
import com.sparta.i_mu.domain.kakao.dto.KakaoResultResponseDto;
import com.sparta.i_mu.domain.kakao.dto.KakaoUserInfo;
import com.sparta.i_mu.domain.kakao.dto.KakaoUserResponseDto;
import com.sparta.i_mu.domain.kakao.dto.KakaoTokenPair;
import com.sparta.i_mu.domain.user.entity.User;
import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.global.util.RedisUtil;
import com.sparta.i_mu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
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
    private final RedisUtil redisUtil;
    private final RestTemplate restTemplate;
    private final KakaoConfig kakaoConfig;


    public KakaoResultResponseDto kakaoLogin(String code) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getAccessToken(code);
        // 2. 토큰으로 카카오 API 호출
        KakaoUserInfo kakaoUserInfo = getKakaoUserInfo(accessToken);

        // 3. 카카오ID로 회원가입 처리
        User kakaoUser = registerKakaoUserIfNeed(kakaoUserInfo);

        //4.JWT 토큰 반환
        KakaoTokenPair kakaoTokenPair = createToken(kakaoUser);

        //5. User 정보는 Dto로 변환 후 반환
        KakaoUserResponseDto kakaoUserDto = KakaoUserResponseDto.builder()
                .userId(kakaoUser.getId())
                .userImage(kakaoUser.getUserImage())
                .email(kakaoUser.getEmail())
                .kakaoId(kakaoUser.getKakaoId())
                .nickname(kakaoUser.getNickname())
                .introduce(kakaoUser.getIntroduce())
                .build();

        return KakaoResultResponseDto.builder()
                .accessToken(kakaoTokenPair.getAccessToken())
                .refreshToken(kakaoTokenPair.getRefreshToken())
                .userInfoResponse(kakaoUserDto)
                .build();
    }


    public String getAccessToken(String code) throws JsonProcessingException {
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

        String clientId = kakaoConfig.getClientId();
        String frontUrl = kakaoConfig.getFrontUrl();
        String clientSecret = kakaoConfig.getClientSecret();

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
        log.info("URI :{} ", uri);
        log.info("HEADER:{} ", headers);
        log.info("BODY:{}", body);

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
        Long id = jsonNode.get("id").asLong();

        JsonNode kakaoAccountNode = jsonNode.get("kakao_account");
        String email = kakaoAccountNode.get("email").asText(); // email 필수동의 값

        JsonNode profileNode = jsonNode.get("kakao_account").get("profile");
        String nickname = profileNode.get("nickname").asText(); // 닉네임은 필수 동의값
        String userImage = null;
        if (profileNode.has("profile_image_url")) {
            userImage = profileNode.get("profile_image_url").asText(null);
        }

        log.info("카카오 사용자 정보: " + id + ", " + nickname + ", " + email + ", " + userImage);

        return KakaoUserInfo.builder()
                .kakaoId(id)
                .email(email)
                .userImage(userImage)
                .nickname(nickname)
                .build();
    }


    public User registerKakaoUserIfNeed(KakaoUserInfo kakaoUserInfo) {
        log.info("중복된 User 확인");

        Long kakaoId = kakaoUserInfo.getKakaoId();

        User kakaoUser = userRepository.findByKakaoId(kakaoId).orElse(null);

        if (kakaoUser == null) {
            // 카카오 사용자 email 동일한 email 가진 회원이 있는지 확인 -> kakaoid는 없지만 같은 email이 있을 경우 = 같은 사람 -> 해당 유저에 kakaoId를 주입
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
                String nickname = kakaoUserInfo.getNickname();

                int suffix = 1;
                while (userRepository.findByNickname(nickname).isPresent()) {
                    nickname = kakaoUserInfo.getNickname() + "_" + suffix++;
                }
                kakaoUser = User.builder()
                        .email(kakaoEmail)
                        .nickname(nickname)
                        .userImage(profile)
                        .password(encodedPassword)
                        .build();
            }
            userRepository.save(kakaoUser);
            log.info("Kakao User정보 Id : {}", kakaoUser.getId());

        }
        return kakaoUser;
    }


    // kakao로그인 유저는 userId 토큰 생성
    private KakaoTokenPair createToken(User kakaoUser) {
        String accessToken = jwtUtil.createAccessToken(kakaoUser.getEmail());
        String refreshToken = jwtUtil.createRefreshToken(kakaoUser.getEmail());

        redisUtil.storeRefreshToken(accessToken, refreshToken);
        return new KakaoTokenPair(accessToken, refreshToken);
    }


    public boolean unlinkKakaoAccount(Long kakaoId) {
        // 카카오 연결 해제 API URL
        String unlinkURL = "https://kapi.kakao.com/v1/user/unlink";

        String adminKey = kakaoConfig.getAdminKey();
        log.info("카카오 연결 해제에 필요한 AdminKey : {}" , adminKey);
        // HTTP 요청 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded" );
        headers.add("Authorization", "KakaoAK " + adminKey);

        LinkedMultiValueMap<String,String> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("target_id_type", "user_id");
        bodyMap.add("target_id", String.valueOf(kakaoId));

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(unlinkURL)
                .headers(headers)
                .body(bodyMap); //바디가 비어있을 때

        try {
            // API 호출
            ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("카카오 소셜 로그인 탈퇴 성공.");
                return true;
            } else {
                log.error("카카오 소셜 로그인 탈퇴 실패. 응답 코드: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("카카오 소셜 로그인 탈퇴 중 오류 발생", e);
            return false;
        }
    }
}