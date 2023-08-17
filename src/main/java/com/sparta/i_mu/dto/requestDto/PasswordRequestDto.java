package com.sparta.i_mu.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PasswordRequestDto {

    @NotBlank(message = "기존 비밀번호는 공백일 수 없습니다.")
    @Size(min = 8, max = 15, message = "기존 비밀번호는 8~15글자 사이여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9'~!@#$%^&*()-_=+]+$", message = "기존 비밀번호는 영어 대소문자, 숫자, 특수문자만 가능합니다.")
    private String originPassword;

    @NotBlank(message = "변경 비밀번호는 공백일 수 없습니다.")
    @Size(min = 8, max = 15, message = "변경 비밀번호는 8~15글자 사이여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9'~!@#$%^&*()-_=+]+$", message = "변경 비밀번호는 영어 대소문자, 숫자, 특수문자만 가능합니다.")
    private String changePassword;

}