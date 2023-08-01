package com.sparta.i_mu.dto.requestDto;

import com.sparta.i_mu.entity.Location;
import com.sparta.i_mu.entity.Song;
import lombok.*;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostRequestDto {
    private List<Location> address;
    private List<Song> songs;
    private String content;
}
