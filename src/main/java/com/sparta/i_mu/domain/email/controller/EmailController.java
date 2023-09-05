package com.sparta.i_mu.domain.email.controller;

import com.sparta.i_mu.domain.email.dto.EmailMessage;
import com.sparta.i_mu.domain.email.dto.EmailRequestDto;
import com.sparta.i_mu.domain.email.service.EmailService;
import com.sparta.i_mu.domain.user.dto.ChangePasswordRequest;
import com.sparta.i_mu.domain.user.service.UserService;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth/email")
@RestController
@RequiredArgsConstructor
@Tag(name = "Email", description = "이메일 인증 API Document")

public class EmailController {

    private final EmailService emailService;
    private final UserService userService;

    // 회원가입 이메일 인증
    @PostMapping("/send-sign")
    @Operation(summary = "회원가입 이메일 인증 번호 전송", description = "회원가입 이메일 인증 번호 전송")
    public ResponseEntity<?> sendEmail_sign(@RequestBody EmailRequestDto emailRequestDto) {

        EmailMessage emailMessage = EmailMessage.builder()
                .email(emailRequestDto.getEmail())
                .subject("[P.PLE] 이메일 인증을 위한 인증 코드 발송")
                .build();

        emailService.sendEmail_sign(emailMessage, emailRequestDto.getEmail(), "email");

        return ResponseEntity.ok("인증 메일을 발송했습니다");
    }


    @PostMapping("/send-pw")
    @Operation(summary = "비밀번호 찾기 이메일 인증 번호 전송", description = "비밀번호 찾기 이메일 인증 번호 전송")
    public ResponseEntity<?> sendEmail_pw(@RequestBody EmailRequestDto emailRequestDto) {

        EmailMessage emailMessage = EmailMessage.builder()
                .email(emailRequestDto.getEmail())
                .subject("[P.PLE] 이메일 인증을 위한 인증 코드 발송")
                .build();

        emailService.sendEmail_pw(emailMessage, emailRequestDto.getEmail(), "email");

        return ResponseEntity.ok("인증 메일을 발송했습니다");
    }

    //회원 가입 시 사용
    @PostMapping("/sign-check")
    @Operation(summary = "이메일 인증 번호 검증", description = "이메일 인증번호 검증")
    public ResponseEntity<?> checkCode(@RequestBody EmailRequestDto emailRequestDto) {
        Boolean check = emailService.verifyEmailCode(emailRequestDto.getEmail(), emailRequestDto.getCode());
        return ResponseEntity.ok(check);
    }

    /*
        1. 비밀번호를 찾기를 위한 이메일 check를 하나 더 만들기
        2. 다음 버튼을 눌렀을 때 -> 인증 확인만 하게 -> redis에서 삭제가 이뤄지면 안됌!
        3. 인증이 되었을 때 -> code랑 email 새로운 newPassword 세개를 보내줘야함. <앞에서 입력한 코드 값을 또 보내주시면 됩니당!>
        4. 세개 가 다 들어왔을 때 다시 code 인증을 하고? 인증이 되면 user를 찾고 user가 있으면 password를 변경 하다.

    */
    //비밀번호 찾기 시 사용
    @PostMapping("/pw-check")
    @Operation(summary = "이메일 인증 번호 검증", description = "이메일 인증번호 검증")
    public ResponseEntity<?> checkCode_pw(@RequestBody EmailRequestDto emailRequestDto) {
        Boolean check = emailService.verifyEmailCode_pw(emailRequestDto.getEmail(), emailRequestDto.getCode());
        return ResponseEntity.ok(check);
    }

    // 비밀 번호 찾기 시
    @PostMapping("/change-password")
    @Operation(summary = "잃어버린 비밀번호 수정", description = "잃어버린 비밀번호 수정")
    public ResponseResource<?> changePassword(@RequestBody ChangePasswordRequest request) {
        return emailService.changePassword(request.getEmail(), request.getNewPassword(), request.getCode());
    }

}

