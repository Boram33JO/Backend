package com.sparta.i_mu.filter;

import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.global.util.RedisUtil;
import com.sparta.i_mu.security.UserDetailsServiceImpl;
import com.sparta.i_mu.service.AuthService;
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
import java.nio.file.AccessDeniedException;

@Slf4j(topic = "JWT 검증 및 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthService authService;
    private final RedisUtil redisUtil;

    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = jwtUtil.getAccessTokenFromRequest(request);
        String refreshToken = jwtUtil.getRefreshTokenFromRequest(request);
        if (StringUtils.hasText(accessToken)) { // accessToken이 없을때
            try {
                if (!authService.isAccessTokenValid(accessToken)) {
                    log.warn("AccessToken 토큰 검증 실패 -> 재발급 시도");
                    renewAccessTokenByRefreshToken(accessToken ,refreshToken, response);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json; charset=UTF-8");
                    response.getWriter().write("토큰이 만료되었습니다. 재발급한 토근으로 재실행 해주세요.");
                    return;
                }
                String userNickname = jwtUtil.getUserInfoFromToken(accessToken).getSubject();
                log.info("info.Subject nickname :{}", userNickname);
                setAuthentication(userNickname);
                authService.refreshTokenRegularly(refreshToken, accessToken, response);
                log.info("info.Subject nickname :{}", userNickname);
            } catch (AccessDeniedException e) {
                log.error(e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("토큰 인증에 실패하였습니다.");
                return;
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        // accessToken 이 없을때
        filterChain.doFilter(request, response);
    }

    private void renewAccessTokenByRefreshToken(String accessToken,String refreshToken, HttpServletResponse response) throws Exception {
        if (refreshToken == null || !authService.isRefreshTokenValid(refreshToken)) { // refreshToken +  accessToken 둘다 없을 경우
            log.error("AccessToken 및 RefreshToken 모두 유효하지 않습니다. 다시 로그인 해주세요.");
            throw new AccessDeniedException("토큰 인증에 실패하였습니다.");
        }
        // refreshToken 이 존재하는 경우
        String newAccessToken = authService.refreshAccessToken(refreshToken);
        response.setHeader(jwtUtil.HEADER_ACCESS_TOKEN, newAccessToken);

        //Redis에서 기존의 accessToken-refreshToken 값 삭제 후 새롭게 저장
        redisUtil.removeRefreshToken("Bearer " + accessToken);
        log.info("삭제한 access : {} " ,"Bearer "+ accessToken);
        log.info("삭제한 refresh : {} " ,"Bearer "+ refreshToken);
        redisUtil.storeRefreshToken(newAccessToken, "Bearer "+ refreshToken);
        log.info("발급한 access : {} " ,newAccessToken);
        log.info("발급한 refresh : {} " ,"Bearer "+ refreshToken);

        String userNickname = jwtUtil.getUserInfoFromToken(jwtUtil.substringToken(newAccessToken)).getSubject();
        setAuthentication(userNickname);
        log.info("재발급 한 AccessToken 의 유저 정보 : {}", userNickname);
    }

    // 인증 처리
    private void setAuthentication(String nickname) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(nickname);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
    //인증 객체 생성
    private Authentication createAuthentication(String nickname) {
        UserDetails jwtUserDetails = userDetailsService.loadUserByUsername(nickname);
        return new UsernamePasswordAuthenticationToken(jwtUserDetails, null, jwtUserDetails.getAuthorities());
    }
}