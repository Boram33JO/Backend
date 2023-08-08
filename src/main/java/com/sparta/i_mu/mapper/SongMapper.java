package com.sparta.i_mu.mapper;

import com.sparta.i_mu.dto.requestDto.SongSaveRequestDto;
import com.sparta.i_mu.dto.responseDto.PostListSongResponseDto;
import com.sparta.i_mu.dto.responseDto.SongResponseDto;
import com.sparta.i_mu.entity.Song;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SongMapper {

    SongMapper SONG_INSTANCE = Mappers.getMapper(SongMapper.class);
    Song requestDtoToEntity(SongSaveRequestDto songSaveRequestDto);
    SongResponseDto entityToResponseDto(Song song);
    PostListSongResponseDto entityToPostListSongResponseDto(Song song);
}
