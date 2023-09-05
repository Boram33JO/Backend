package com.sparta.i_mu.domain.post.dto;

import com.sparta.i_mu.domain.song.dto.SongSaveRequestDto;
import lombok.*;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 게시글을 등록을 위해 필요한 정보
public class PostSaveRequestDto {

    private Double latitude;
    private Double longitude;
    private String address;
    private String placeName;
    private List<SongSaveRequestDto> songs;
    private String postTitle;
    private String content;
    private Long category;
}
