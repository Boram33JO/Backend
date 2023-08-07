package com.sparta.i_mu.dto.requestDto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class PasswordRequestDto {

    @Pattern(regexp = "[a-zA-Z0-9'~!@#$%^&*()-_=+]{8,15}")  // 특수문자 포함되도록 변경
    private String originPassword;

    @Pattern(regexp = "[a-zA-Z0-9'~!@#$%^&*()-_=+]{8,15}")  // 특수문자 포함되도록 변경
    private String changePassword;

}