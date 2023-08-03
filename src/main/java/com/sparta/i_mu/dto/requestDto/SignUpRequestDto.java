package com.sparta.i_mu.dto.requestDto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequestDto {

    @Pattern(regexp = "[a-zA-Z0-9ㄱ-ㅎ가-힣]{2,10}")
    private String nickname;  //loginId

    @Pattern(regexp = "[a-zA-Z0-9'~!@#$%^&*()-_=+]{8,15}")  // 특수문자 포함되도록 변경
    private String password;

    private String email;

    private boolean admin = false;
    private String adminToken = "";

}