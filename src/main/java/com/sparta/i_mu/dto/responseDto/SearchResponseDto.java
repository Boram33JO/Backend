package com.sparta.i_mu.dto.responseDto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchResponseDto {
    private List<SongResponseDto> songs;
    private List<PostResponseDto> posts;
    private List<UserResponsDto> users;
    private List<PostResponseDto> locations;
}
