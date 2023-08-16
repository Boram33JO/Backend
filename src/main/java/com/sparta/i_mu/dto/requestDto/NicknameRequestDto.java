package com.sparta.i_mu.dto.requestDto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class NicknameRequestDto {
    @Pattern(regexp = "[a-zA-Z0-9ㄱ-ㅎ가-힣]{2,10}")
    private String nickname;
}
