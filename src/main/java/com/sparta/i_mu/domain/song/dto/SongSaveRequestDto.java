package com.sparta.i_mu.domain.song.dto;

import lombok.Getter;

@Getter
public class SongSaveRequestDto {
    private String songNum;
    private String artistName;
    private String songTitle;
    private String album;
    private String audioUrl;
    private String thumbnail;
    private String externalUrl;
}
