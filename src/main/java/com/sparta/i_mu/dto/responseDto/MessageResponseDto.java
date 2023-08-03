package com.sparta.i_mu.dto.responseDto;

import lombok.Getter;

@Getter
public class MessageResponseDto {
    private String message;
    private String statusCode;

    public MessageResponseDto(String message, String statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }
}