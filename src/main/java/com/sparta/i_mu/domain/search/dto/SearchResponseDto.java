package com.sparta.i_mu.domain.search.dto;

import com.sparta.i_mu.domain.post.dto.PostResponseDto;
import com.sparta.i_mu.domain.song.dto.SongResponseDto;
import com.sparta.i_mu.domain.user.dto.UserInfoResponseDto;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchResponseDto {
    private List<SongResponseDto> songs;
    private List<PostResponseDto> posts;
    private List<UserInfoResponseDto> users;
    private List<PostResponseDto> locations;
}
