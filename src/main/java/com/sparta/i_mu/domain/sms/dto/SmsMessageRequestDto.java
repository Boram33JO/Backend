package com.sparta.i_mu.domain.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//sms
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class SmsMessageRequestDto {

    private String to;

}