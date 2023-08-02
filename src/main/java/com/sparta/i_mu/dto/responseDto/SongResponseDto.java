package com.sparta.i_mu.dto.responseDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SongResponseDto {
    private String artistName;
    private String title;
    private String album;
    private String thumbnail;
    private String external_url;
}
