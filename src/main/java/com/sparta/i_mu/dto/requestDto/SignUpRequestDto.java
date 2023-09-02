package com.sparta.i_mu.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequestDto {

    @NotBlank(message = "닉네임은 공백일 수 없습니다.")
    @Size(min = 2, max = 12, message = "닉네임은 2~12글자 사이여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9ㄱ-ㅎ가-힣]+$", message = "닉네임은 영어 대소문자, 숫자, 한글만 가능합니다.")
    private String nickname;

    @NotBlank(message = "비밀번호는 공백일 수 없습니다.")
    @Size(min = 8, max = 15, message = "비밀번호는 8~15글자 사이여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9'~!@#$%^&*()-_=+]+$", message = "비밀번호는 영어 대소문자, 숫자, 특수문자만 가능합니다.")  // 특수문자 포함되도록 변경
    private String password;

    private String email; //loginId
    private String phonenumber;

    private boolean admin = false;
    private String adminToken = "";
}