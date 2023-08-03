package com.sparta.i_mu.mapper;

import com.sparta.i_mu.dto.responseDto.SongResponseDto;
import com.sparta.i_mu.entity.PostSongLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PostSongLinkMapper {
    PostSongLinkMapper POST_SONG_LINK_INSTANCE = Mappers.getMapper(PostSongLinkMapper.class);
    @Mapping(source = "postSongLink.song", target = "song")
    SongResponseDto postSongListEntityToSongResponseDto(PostSongLink postSongLink);
}
