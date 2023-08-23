package com.sparta.i_mu.dto.requestDto;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class EmailPostDto {
    private String email;
    private String code;
}

