package com.sparta.i_mu.filter;

import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.security.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;

@Slf4j(topic = "JWT 검증 및 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    @Autowired
    private RequestMappingInfoHandlerMapping handlerMapping;

    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping();
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 로그인 경로는 필터를 회피
        if (request.getRequestURI().equals("/api/user/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = jwtUtil.getAccessTokenFromRequest(request); // good
        if (StringUtils.hasText(accessToken) && jwtUtil.validateAccessToken(accessToken)) {
            Claims info = jwtUtil.getUserInfoFromToken(accessToken);
            try {
                setAuthentication(info.getSubject());
            } catch (Exception e) {
                log.error(e.getMessage());
                return;
            }
        } else {
            String refreshToken = jwtUtil.getRefreshTokenFromRequest(request);
            if(StringUtils.hasText(refreshToken) && jwtUtil.validateRefreshToken(refreshToken)){
                // 새로운 AccessToken 발급
                String newAccessToken = jwtUtil.regenerateAccessToken(refreshToken,response);
                log.info("재발급 토큰 : {}",newAccessToken);
                Claims info = jwtUtil.getUserInfoFromToken(newAccessToken);
                try {
                    setAuthentication(info.getSubject());
                } catch (Exception e) {
                    log.error(e.getMessage());
                    return;
                }
            } else {
                // 둘 다 유효하지 않은 경우
                log.error("AccessToken 및 RefreshToken 모두 유효하지 않습니다.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"error\": \"AccessToken 및 RefreshToken 모두 유효하지 않습니다. 다시 로그인 해주세요.\"}");
                return;
            }
        }
        filterChain.doFilter(request,response);
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