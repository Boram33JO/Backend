
package com.sparta.i_mu.domain.sms.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.i_mu.domain.sms.dto.SmsMessageRequestDto;
import com.sparta.i_mu.domain.sms.dto.SmsRequestDto;
import com.sparta.i_mu.domain.sms.dto.SmsResponseDto;
import com.sparta.i_mu.domain.sms.service.SmsService;
import com.sparta.i_mu.domain.user.service.UserService;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
//sms
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth/sms")
@Tag(name = "Sms", description = "휴대폰 인증 API Document")
public class SmsController {

    private final SmsService smsService;
    private final UserService userService;

    @PostMapping("/send-sign")
    @Operation(summary = "회원가입 휴대폰 문자 인증 보내기", description ="회원가입 휴대폰 문자 인증번호 보내기")
    public ResponseResource<?> sendSign(@RequestBody SmsMessageRequestDto smsMessageRequestDto) throws UnsupportedEncodingException, URISyntaxException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        SmsResponseDto smsResponseDto = smsService.sendSign(smsMessageRequestDto);
        return ResponseResource.data(smsResponseDto,HttpStatus.OK,"인증 문자가 발급되었습니다.");
    }

    @PostMapping("/send-id")
    @Operation(summary = "아이디 찾기 휴대폰 문자 인증 보내기", description ="아이디 찾기 휴대폰 문자 인증번호 보내기")
    public ResponseResource<?> sendPw(@RequestBody SmsMessageRequestDto smsMessageRequestDto) throws UnsupportedEncodingException, URISyntaxException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        SmsResponseDto smsResponseDto = smsService.sendPw(smsMessageRequestDto);
        return ResponseResource.data(smsResponseDto,HttpStatus.OK,"인증 문자가 발급되었습니다.");
    }

    @PostMapping("/check")
    @Operation(summary = "휴대폰 문자 인증 번호검증", description ="휴대폰 문자 인증번호 검증")
    public ResponseEntity<?> verifyPhoneCode_signUp(@RequestBody SmsRequestDto smsRequestDto){
        Boolean check = smsService.verifyPhoneCode_signUp(smsRequestDto.getPhoneNumber(), smsRequestDto.getSmsConfirmNum());
        return ResponseEntity.ok(check);
    }

    @Operation(summary = "잃어버린 아이디 찾기", description = "잃어버린 아이디 찾기")
    @PostMapping("/find-email")
    public ResponseResource<?> verifyPhoneCode_findEmail(@RequestBody SmsRequestDto smsRequestDto){
        return smsService.verifyPhoneCode_findEmail(smsRequestDto.getPhoneNumber(), smsRequestDto.getSmsConfirmNum());
    }
}






