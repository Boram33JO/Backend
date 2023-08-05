package com.sparta.i_mu.dto.requestDto;

import lombok.Getter;

@Getter
public class SongSaveRequestDto {
    private String songId;
    private String artist;
    private String title;
    private String album;
    private String thumbnail;
}
