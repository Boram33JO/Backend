package com.sparta.i_mu.domain.song.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongByCategoryResponseDto {
    private Long category;
    private List<SongResponseDto> songResponseDtos;
}
