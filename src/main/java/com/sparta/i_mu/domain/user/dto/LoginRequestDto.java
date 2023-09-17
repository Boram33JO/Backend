package com.sparta.i_mu.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequestDto {

    private String password;

    private String email;

}