package com.sparta.i_mu.dto.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class SearchMainResponseDto {

    private List<SongByCategoryResponseDto> topSongs;
    private List<PostResponseDto> topPosts;
    private List<String> topLocations;
    private List<String> topSearchKeywords;
}
