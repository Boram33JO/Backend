package com.sparta.i_mu.controller;

import com.sparta.i_mu.dto.responseDto.SongByCategoryResponseDto;
import com.sparta.i_mu.dto.responseDto.SongResponseDto;
import com.sparta.i_mu.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
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

    @GetMapping("/mostSong")
    public List<SongByCategoryResponseDto> getMostPostSong(){
        return songService.getMostPostSong();
    }
}
