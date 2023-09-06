package com.sparta.i_mu.domain.email.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailMessage {

    private String email;
    private String subject;
    private String message;
    private String content;
}