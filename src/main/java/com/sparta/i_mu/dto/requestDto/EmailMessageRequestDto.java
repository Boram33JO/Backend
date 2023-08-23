package com.sparta.i_mu.dto.requestDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailMessageRequestDto {

    private String to;
    private String subject;
    private String message;
}
