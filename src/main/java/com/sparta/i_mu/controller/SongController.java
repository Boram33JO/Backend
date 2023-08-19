package com.sparta.i_mu.controller;

import com.sparta.i_mu.dto.responseDto.SongByCategoryResponseDto;
import com.sparta.i_mu.dto.responseDto.SongResponseDto;
import com.sparta.i_mu.service.SongService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Song", description = "Song API")
@RequestMapping("/api/song")
public class SongController {

    private final SongService songService;

    /**
     * spotify 노래 검색 기능
     * @param keyword
     * @return 해당 keyword에 맞는 아티스트의 노래
     */
    @GetMapping("/search")
    public List<SongResponseDto> getSearch(@RequestParam String keyword){
        return songService.getSearch(keyword);
    }

    @GetMapping("/AllMostSong")
    public List<SongResponseDto> getMostAllPostSong(){
        return songService.getMostAllPostSong();
    }
    /**
     * 카테고리별 별 가장 많이 포스팅된 노래 top 4
     * @return
     */
    @GetMapping("/mostSong")
    public List<SongByCategoryResponseDto> getMostByCategoryPostSong(){
        return songService.getMostByCategoryPostSong();
    }
}
