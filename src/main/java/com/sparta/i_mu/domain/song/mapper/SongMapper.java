package com.sparta.i_mu.domain.song.mapper;

import com.sparta.i_mu.domain.song.dto.SongSaveRequestDto;
import com.sparta.i_mu.domain.post.dto.PostListSongResponseDto;
import com.sparta.i_mu.domain.song.dto.SongResponseDto;
import com.sparta.i_mu.domain.song.entity.Song;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SongMapper {

    SongMapper SONG_INSTANCE = Mappers.getMapper(SongMapper.class);
    @Mapping(target = "id", ignore = true)
    Song requestDtoToEntity(SongSaveRequestDto songSaveRequestDto);
    SongResponseDto entityToResponseDto(Song song);
    PostListSongResponseDto entityToPostListSongResponseDto(Song song);
}
