package com.sparta.i_mu.dto.requestDto;

import com.sparta.i_mu.entity.Location;
import lombok.Getter;

@Getter
public class PostSearchRequestDto {
    private Double latitude;
    private Double longitude;
}
