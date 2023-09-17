package com.sparta.i_mu.domain.song.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SongByCategoryResponseDto {
    private Long Category;
    private List<SongResponseDto> songResponseDtos;
}
