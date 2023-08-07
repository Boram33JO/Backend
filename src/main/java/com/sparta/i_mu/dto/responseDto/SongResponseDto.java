package com.sparta.i_mu.dto.responseDto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SongResponseDto {
    private Long id;
    private String songNum;
    private String artistName;
    private String songTitle;
    private String album;
    private String audioUrl;
    private String thumbnail;
    private String externalUrl;
}
