package com.sparta.i_mu.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ChangePasswordRequest {

    private String email;

    @NotBlank(message = "비밀번호는 공백일 수 없습니다.")
    @Size(min = 8, max = 15, message = "비밀번호는 8~15글자 사이여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9'~!@#$%^&*()-_=+]+$", message = "기존 비밀번호는 영어 대소문자, 숫자, 특수문자만 가능합니다.")
    private String newPassword;

    private String code;

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
