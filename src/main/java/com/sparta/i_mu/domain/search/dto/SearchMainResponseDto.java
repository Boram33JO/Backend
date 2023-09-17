package com.sparta.i_mu.domain.search.dto;

import com.sparta.i_mu.domain.post.dto.PostResponseDto;
import com.sparta.i_mu.domain.song.dto.SongByCategoryResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class SearchMainResponseDto {

    private List<SongByCategoryResponseDto> topSongs;
    private List<PostResponseDto> topPosts;
    private List<String> topLocations;
    private Set<String> topSearchKeywords;
}
