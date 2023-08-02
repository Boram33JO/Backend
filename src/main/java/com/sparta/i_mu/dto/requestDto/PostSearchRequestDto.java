package com.sparta.i_mu.dto.requestDto;

import com.sparta.i_mu.entity.Location;
import lombok.Getter;

@Getter
public class PostSearchRequestDto {
    private Location location;
    private Boolean locationAgreed;
    private String category;
}
