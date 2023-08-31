package com.sparta.i_mu.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.i_mu.filter.JwtAuthenticationFilter;
import com.sparta.i_mu.filter.JwtAuthorizationFilter;
import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.security.UserDetailsServiceImpl;
import com.sparta.i_mu.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;


import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final ObjectMapper objectMapper;
    private final WebConfig webConfig;
    private final RedisUtil redisUtil;

    private static final String[] SWAGGER_WHITELIST = {
            "/api/swagger-ui/**", "/api-docs/**", "/swagger-ui.html", "/api/swagger/**"
    };

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //AuthenticationManager 를 만들고 등록하는 코드
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception{
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, objectMapper, redisUtil);
        filter.setAuthenticationManager(authenticationManager(authenticationConfiguration));
        filter.setFilterProcessesUrl("/user/login");
        return filter;
    }
    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter(){
        return new JwtAuthorizationFilter(jwtUtil, userDetailsService, redisUtil);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(WebSecurityConfig::stateless)
                .authorizeHttpRequests(authorizationRequest -> {
                    authorizationRequest
                            .requestMatchers("/user/login", "/user/signup").permitAll() //로그인, 회원가입
                            .requestMatchers(GET,"/user/**").permitAll()
                            .requestMatchers(POST,"/user/logout").permitAll()
                            .requestMatchers(POST,"/user/check").permitAll()
                            .requestMatchers(POST,"/token/refresh").permitAll() // 액세스토큰 재발급 요청
                            .requestMatchers(POST, "/oauth/**").permitAll() // 소셜 로그인
                            .requestMatchers(GET,"/posts/**").permitAll() //게시글 조회,댓글조회
                            .requestMatchers(POST,"/posts/map/**").permitAll()//지도 조회
                            .requestMatchers(GET, "/song/**").permitAll()// 노래 검색 및 조회
                            .requestMatchers(GET, "/top-follows").permitAll()
                            .requestMatchers(POST, "/auth/**").permitAll()
                            .requestMatchers(POST, "/sms/**").permitAll()
                            .requestMatchers(POST, "/user/change-password/**").permitAll()
                            .requestMatchers(SWAGGER_WHITELIST).permitAll()
                            .anyRequest().authenticated();
                })
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(accessDeniedHandler())
                        .authenticationEntryPoint(authenticationEntryPoint())
                )

                .addFilter(webConfig.corsFilter())
                .addFilterBefore(jwtAuthorizationFilter(), JwtAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authenticationException) -> {
            response.getWriter().write(authenticationException.getMessage());
        };

    }
    private AccessDeniedHandler accessDeniedHandler() {
        return ((request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write(accessDeniedException.getMessage());
        });
    }

    private static void stateless(SessionManagementConfigurer<HttpSecurity> SessionManagementConfigurer) {
        SessionManagementConfigurer.sessionCreationPolicy(STATELESS);
    }

}