package com.sparta.i_mu.mapper;

import com.sparta.i_mu.dto.responseDto.SongResponseDto;
import com.sparta.i_mu.entity.PostSongLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PostSongLinkMapper {
    PostSongLinkMapper POST_SONG_LINK_INSTANCE = Mappers.getMapper(PostSongLinkMapper.class);

    @Mapping(source = "postSongLink.song.artistName", target = "artistName")
    @Mapping(source = "postSongLink.song.title", target = "title")
    @Mapping(source = "postSongLink.song.album", target = "album")
    @Mapping(source = "postSongLink.song.thumbnail", target = "thumbnail")
    @Mapping(source = "postSongLink.song.external_url", target = "external_url")
    SongResponseDto entityToDto(PostSongLink postSongLink);
}
