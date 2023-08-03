package com.sparta.i_mu.dto.requestDto;

import com.sparta.i_mu.entity.Location;
import lombok.*;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostSaveRequestDto {

    private Double latitude;
    private Double longitude;
    private String address;
    private List<SongSaveRequestDto> songs;
    private String content;
    private String category;
}
