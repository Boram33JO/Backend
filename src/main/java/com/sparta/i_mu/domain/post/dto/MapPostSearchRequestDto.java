package com.sparta.i_mu.domain.post.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class MapPostSearchRequestDto {
    @Min(-90)
    @Max(90)
    private Double latitude;

    @Min(-180)
    @Max(180)
    private Double longitude;
}
