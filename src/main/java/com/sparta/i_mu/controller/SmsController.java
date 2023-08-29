
package com.sparta.i_mu.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.i_mu.dto.requestDto.SmsDto;
import com.sparta.i_mu.dto.requestDto.MessageDto;
import com.sparta.i_mu.dto.responseDto.SmsResponseDto;
import com.sparta.i_mu.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/sms")
@Tag(name = "Sms", description = "휴대폰 인증번호 받아오기")
public class SmsController {

    private final SmsService smsService;

//    @PostMapping("/send")
//    public SmsResponseDto sendSms(@RequestBody MessageDto messageDto) throws UnsupportedEncodingException, URISyntaxException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
//        SmsResponseDto responseDto = smsService.sendSms(messageDto);
//        return responseDto;
//    }

    @PostMapping("/send")
    @Operation(summary = "전화번호 문자 인증 보내기", description ="전화번호 문자 인증번호 보내기")
    public ResponseEntity<?> sendSms(@RequestBody MessageDto messageDto) throws UnsupportedEncodingException, URISyntaxException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        SmsResponseDto responseDto = smsService.sendSms(messageDto);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/check")
    @Operation(summary = "전화번호 문자 인증 번호검증", description ="전화번호 문자 인증번호 검증")
    public ResponseEntity<?> verifyPhoneCode(@RequestBody SmsDto smsDto){
        Boolean check = smsService.verifyPhoneCode(smsDto.getTo(),smsDto.getSmsConfirmNum());
        return ResponseEntity.ok(check);
    }
}





