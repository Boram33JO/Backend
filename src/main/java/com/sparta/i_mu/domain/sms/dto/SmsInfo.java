package com.sparta.i_mu.domain.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class SmsInfo {
    private String type;
    private String contentType;
    private String countryCode;
    private String from;
    private String content;
    private List<SmsMessageRequestDto> messages;
}