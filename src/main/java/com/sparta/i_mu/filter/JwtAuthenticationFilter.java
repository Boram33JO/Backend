package com.sparta.i_mu.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.i_mu.dto.requestDto.LoginRequestDto;
import com.sparta.i_mu.dto.responseDto.LoginResponseDto;
import com.sparta.i_mu.dto.responseDto.MessageResponseDto;
import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.repository.UserRepository;
import com.sparta.i_mu.security.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private UserRepository userRepository;


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
                        null // TODO 리펙토링 가능한지 찾아보기
                )
        );
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        String username = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();
        String nickname = ((UserDetailsImpl) authResult.getPrincipal()).getNickname();
        String userImage = ((UserDetailsImpl) authResult.getPrincipal()).getUserImage();

        String token = jwtUtil.createAccessToken(username);
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, token);

        LoginResponseDto responseDto = new LoginResponseDto(nickname, userImage);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(responseDto));

    }


    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(401);
        MessageResponseDto responseDto = new MessageResponseDto("로그인 실패", HttpStatus.UNAUTHORIZED.toString()); //ok는 200 성공 코드
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(responseDto));
    }

}