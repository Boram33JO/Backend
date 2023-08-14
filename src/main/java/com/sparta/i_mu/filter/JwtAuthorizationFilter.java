package com.sparta.i_mu.filter;

import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.security.UserDetailsServiceImpl;
import com.sparta.i_mu.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;

@Slf4j(topic = "JWT 검증 및 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthService authService;

    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping();
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = jwtUtil.getAccessTokenFromRequest(request);
        if(StringUtils.hasText(accessToken)){
            try {
                String refreshToken = jwtUtil.getRefreshTokenFromRequest(request);
                String userEmail = null;

                if (authService.isAccessTokenValid(accessToken)) {
                    log.info("토큰 검증 확인 결과 : {} ", authService.isAccessTokenValid(accessToken));
                    userEmail = getUserFromValidAccessToken(accessToken);
                } else {
                    userEmail = renewAccessTokenByRefreshToken(refreshToken, response);
                }
// 리프레시 토큰이 일주일 이상 된 경우, 새로운 리프레시 토큰을 발급하고 응답 헤더에 설정합니다.
                authService.refreshTokenRegularly(refreshToken, userEmail, response);
                filterChain.doFilter(request, response);
            }
            catch (Exception e) {
                log.error("필터 처리 중 오류 발생 : ", e);
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생하였습니다.");
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getUserFromValidAccessToken(String accessToken) {
        Claims info = jwtUtil.getUserInfoFromToken(accessToken);
        setAuthentication(info.getSubject());
        try {
            setAuthentication(info.getSubject());
            log.info("accessToken 의 유저 정보 : {}", info.getSubject());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return info.getSubject();
    }

    private String renewAccessTokenByRefreshToken (String refreshToken, HttpServletResponse response) throws Exception {
        if (refreshToken == null) { // refreshToken +  accessToken 둘다 없을 경우
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "AccessToken 및 RefreshToken 모두 유효하지 않습니다. 다시 로그인 해주세요.");
        }
        // refreshToken 이 존재하는 경우
        String newAccessToken = authService.refreshAccessToken(refreshToken);
        response.setHeader(jwtUtil.HEADER_ACCESS_TOKEN, newAccessToken);

        Claims info = jwtUtil.getUserInfoFromToken(jwtUtil.substringToken(newAccessToken));
        setAuthentication(info.getSubject());
        log.info("newAccessToken 의 유저 정보 : {}", info.getSubject());
        return info.getSubject();
    }

    // ErrorResponse ?
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String errorMessage) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"" + errorMessage + "\"}");
    }

    // 인증 처리
    private void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    //인증 객체 생성
    private Authentication createAuthentication(String email) {
        UserDetails jwtUserDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(jwtUserDetails, null, jwtUserDetails.getAuthorities());
    }
}