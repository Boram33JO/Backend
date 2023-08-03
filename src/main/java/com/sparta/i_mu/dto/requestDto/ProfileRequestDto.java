package com.sparta.i_mu.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequestDto {

    private String password;

    private String nickname;

    private String introduce;

}
