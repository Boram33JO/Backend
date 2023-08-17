package com.sparta.i_mu.global.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    public final String HEADER_ACCESS_TOKEN = "AccessToken";
    public final String HEADER_REFRESH_TOKEN = "RefreshToken";

    public static final String AUTHORIZATION_KEY = "auth";
    private final String BEARER = "Bearer ";
    private final Long ACCESS_TOKEN_EXPIRATION_TIME = 60 * 60 * 1000L; // 1시간 / 1분
    private final Long REFRESH_TOKEN_EXPIRATION_TIME = 14 * 24 * 60 * 60 * 1000L; // 2주 / 10분
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    public static final Logger logger = LoggerFactory.getLogger("JWT 관련 로그");
    private Key key;
    @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey
    private String secretKey;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    /**
     *
     * @param nickname
     * @return
     */
    public String createAccessToken(String nickname) {
        Date date = new Date();
        log.info("createAccessToken");
        return BEARER +
                Jwts.builder()
                        .setSubject(nickname) // 토큰(사용자) 식별자 값
                        .setExpiration(new Date(date.getTime() + ACCESS_TOKEN_EXPIRATION_TIME)) // 만료일
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘, 시크릿 키
                        .compact();

    }//                        .claim(AUTHORIZATION_KEY,role)

    public String createRefreshToken(String nickname) {
        Date date = new Date();
        log.info("createRefreshToken");
        return BEARER +
                Jwts.builder()
                        .setIssuedAt(date) // 등록 날
                        .setSubject(nickname) // 토큰(사용자) 식별자 값
                        .setExpiration(new Date(date.getTime() + REFRESH_TOKEN_EXPIRATION_TIME)) // 만료일
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘, 시크릿 키
                        .compact();
    }

    public void addTokenToHeader(String accessToken, String refreshToken, HttpServletResponse response) {
        response.setHeader(HEADER_ACCESS_TOKEN, accessToken);
        response.setHeader(HEADER_REFRESH_TOKEN, refreshToken);
    }

    // accessToken 가져오기
    public String getAccessTokenFromRequest(HttpServletRequest req) {
        String accessToken = req.getHeader(HEADER_ACCESS_TOKEN);
        if (StringUtils.hasText(accessToken)) {
            return substringToken(accessToken);
        }
        return null;
    }

    // refreshToken 가져오기
    public String getRefreshTokenFromRequest(HttpServletRequest req) {
        String refreshToken = req.getHeader(HEADER_REFRESH_TOKEN);
        if (StringUtils.hasText(refreshToken)) {
            return substringToken(refreshToken);
        }
        return null;
    }

    /**
     * JWT Bearer Substirng 메서드
     *
     * @param token
     * @return subString으로 추출된 token 값
     */
    public String substringToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith(BEARER)) {
            return token.substring(7);
        }
        throw new NullPointerException("토큰의 값이 존재하지 않습니다.");
    }

    public boolean validateAccessToken(String AccessToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(AccessToken); // key로 accessToken 검증
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT accessToken, 만료된 JWT accessToken 입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT accessToken, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    /**
     * JWT 검증 메서드
     *
     * @param
     * @return 토큰 검증 여부
     */
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(refreshToken); // key로 accessToken 검증
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT refreshToken, 만료된 JWT refreshToken 입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT refreshToken, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    public Claims getUserInfoFromToken(String token) {
        log.info("user 의 정보 가져오는 메서드 실행");
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody(); //body부분의 claims를 가지고 올 수 잇음
    }
}