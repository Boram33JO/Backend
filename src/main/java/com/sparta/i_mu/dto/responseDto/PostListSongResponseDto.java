package com.sparta.i_mu.dto.responseDto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostListSongResponseDto {
    private String artistName;
    private String songTitle;
    private String thumbnail;
}
