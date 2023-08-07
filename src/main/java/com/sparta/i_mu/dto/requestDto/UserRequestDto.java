package com.sparta.i_mu.dto.requestDto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {

    @Pattern(regexp = "[a-zA-Z0-9ㄱ-ㅎ가-힣]{2,10}")
    private String nickname;

    private String introduce;

}
