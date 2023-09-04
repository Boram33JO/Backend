package com.sparta.i_mu.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//sms
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class MessageDto {
    private String to;
    private String find;
}