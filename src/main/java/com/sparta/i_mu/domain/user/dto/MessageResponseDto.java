package com.sparta.i_mu.domain.user.dto;

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