package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.requestDto.LoginRequestDto;
import com.sparta.i_mu.dto.requestDto.SignUpRequestDto;
import com.sparta.i_mu.dto.responseDto.MessageResponseDto;
import com.sparta.i_mu.entity.User;
import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }


    // 회원가입 서비스
    public ResponseEntity<MessageResponseDto> createUser(SignUpRequestDto signUpRequestDto){
        String nickname = signUpRequestDto.getNickname();
        String password = passwordEncoder.encode(signUpRequestDto.getPassword());
        String email = signUpRequestDto.getEmail();

        // 회원 중복 확인
        Optional<User> checkNickname = userRepository.findByNickname(nickname);
        if (checkNickname.isPresent()) {
            throw new IllegalArgumentException("중복된 nickname 입니다.");
        }



        // 사용자 등록
        User user = new User(nickname, password, email);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponseDto("회원가입 되었습니다.", HttpStatus.OK.toString()));
    }

    //  로그인 서비스
    public ResponseEntity<MessageResponseDto> loginUser(LoginRequestDto requestDto, HttpServletResponse response){
        String email = requestDto.getEmail();
        String password = requestDto.getPassword();

        // 사용자 이메일 확인
        User user = userRepository.findByNickname(email).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 사용자입니다.")       );

        // 비밀번호 확인
        if(!passwordEncoder.matches(password,user.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // JWT 생성
        String token = jwtUtil.createAccessToken(user.getNickname());


        Cookie cookie = jwtUtil.tokenToCookie(token);

        if(cookie == null)
            throw new IllegalArgumentException("쿠키 생성 실패");

        response.addCookie(cookie);

        return ResponseEntity.ok(new MessageResponseDto("로그인했습니다.", HttpStatus.OK.toString()));
    }
}