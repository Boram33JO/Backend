package com.sparta.i_mu.domain.sms.dto;

import lombok.Getter;

//sms
@Getter
public class SmsRequestDto {
    private String smsConfirmNum;
    private String phoneNumber;
}