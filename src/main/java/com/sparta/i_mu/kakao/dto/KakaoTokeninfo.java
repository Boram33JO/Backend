package com.sparta.i_mu.kakao.dto;

import lombok.Data;

@Data
public class KakaoTokeninfo {
    Long id;
    Integer expires_in;
    Integer app_id;
}