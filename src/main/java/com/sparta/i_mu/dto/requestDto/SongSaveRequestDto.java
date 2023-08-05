package com.sparta.i_mu.dto.requestDto;

import lombok.Getter;

@Getter
public class SongSaveRequestDto {
    private String songNum;
    private String artistName;
    private String title;
    private String album;
    private String audioUrl;
    private String thumbnail;
    private String externalUrl;
}
