package com.sparta.i_mu.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.i_mu.dto.requestDto.LoginRequestDto;
import com.sparta.i_mu.dto.responseDto.LoginResponseDto;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.security.UserDetailsImpl;
import com.sparta.i_mu.global.util.RedisUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;


@Slf4j(topic = "로그인 및 JWT 생성")
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final RedisUtil redisUtil;
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("로그인 시도");
        try {
            LoginRequestDto requestDto = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);
            return authenticationRequest(requestDto);
        } catch (IOException e) {
            throw new RuntimeException("");
        }

    }

    private Authentication authenticationRequest(LoginRequestDto requestDto) {
        return getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.getEmail(),
                        requestDto.getPassword(),
                        null
                )
        );
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        log.info("로그인 성공 및 JWT 생성");
        String nickname = ((UserDetailsImpl) authResult.getPrincipal()).getNickname();
        String email = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();
        String userImage = ((UserDetailsImpl) authResult.getPrincipal()).getUserImage();
        Long userId = ((UserDetailsImpl) authResult.getPrincipal()).getUserId();
        String introduce = ((UserDetailsImpl) authResult.getPrincipal()).getIntroduce();

        String accessToken = jwtUtil.createAccessToken(email);
        log.info("accessToken 발급 : {}",accessToken);
        String refreshToken = jwtUtil.createRefreshToken(email); // username = email
        log.info("refreshToken 발급 : {}",refreshToken);
        redisUtil.storeRefreshToken(accessToken,refreshToken); // refreshToken redis에 저장

        LoginResponseDto loginResponseDto = new LoginResponseDto(nickname, email, userImage, introduce, userId);
        ResponseResource<?> responseDto = new ResponseResource<>(true,loginResponseDto,"로그인 성공", HttpStatus.OK.value(),"null");


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(responseDto));

        jwtUtil.addTokenToHeader(accessToken,refreshToken,response);

    }


    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.info("로그인 실패");

        response.setStatus(401);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"로그인 실패");

    }
}
