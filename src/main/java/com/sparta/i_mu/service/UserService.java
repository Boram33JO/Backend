package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.requestDto.LoginRequestDto;
import com.sparta.i_mu.dto.requestDto.SignUpRequestDto;
import com.sparta.i_mu.dto.responseDto.MessageResponseDto;
import com.sparta.i_mu.entity.User;
import com.sparta.i_mu.entity.UserRoleEnum;
import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor //의존성 주입
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 서비스
    public ResponseEntity<MessageResponseDto> createUser(SignUpRequestDto signUpRequestDto){
        String nickname = signUpRequestDto.getNickname();
        String password = passwordEncoder.encode(signUpRequestDto.getPassword());
        String email = signUpRequestDto.getEmail();


//        checkDuplicatedEmail(email);
//        UserRoleEnum role = UserRoleEnum.USER;
//
//        private void checkDuplicatedEmail(String email) {
//            Optional<User> found = userRepository.findByEmail(email);
//            if (found.isPresent()) {
//                throw new InvalidConditionException(ErrorCodeEnum.DUPLICATE_USERNAME_EXIST);
//            }
//        }

        // 회원 email 중복 확인
        Optional<User> checkEmail = userRepository.findByEmail(email);
        if (checkEmail.isPresent()) {
            throw new IllegalArgumentException("중복된 email 입니다.");
        }

        // 회원 nickname 중복 확인
        Optional<User> checkNickname = userRepository.findByNickname(nickname);
        if (checkNickname.isPresent()) {
            throw new IllegalArgumentException("중복된 nickname 입니다.");
        }

        // 사용자 등록

        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password(password)
//                .role(role)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponseDto("회원가입 되었습니다.", HttpStatus.OK.toString()));
    }

}