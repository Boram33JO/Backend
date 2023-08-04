package com.sparta.i_mu.dto.responseDto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SongResponseDto {
    private Long id;
    private String artistName;
    private String title;
    private String album;
    private String thumbnail;
    private String external_url;
}
