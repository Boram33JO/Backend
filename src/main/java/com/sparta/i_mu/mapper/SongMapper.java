package com.sparta.i_mu.mapper;

import com.sparta.i_mu.dto.requestDto.SongSaveRequestDto;
import com.sparta.i_mu.entity.Song;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SongMapper {

    SongMapper SONG_INSTANCE = Mappers.getMapper(SongMapper.class);
    Song responseDtoToEntity(SongSaveRequestDto songSaveRequestDto);
}
