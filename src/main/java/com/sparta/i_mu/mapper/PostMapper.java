package com.sparta.i_mu.mapper;

import com.sparta.i_mu.dto.responseDto.PostResponseDto;
import com.sparta.i_mu.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper(uses = {PostSongLinkMapper.class})// PostSongLink 리스트를 SongResponseDto 리스트로 변환해야 함
public interface PostMapper {
    PostMapper POST_INSTANCE = Mappers.getMapper(PostMapper.class);

    @Mapping(source = "post.category.name", target = "category")
    @Mapping(source = "post.postSongLink", target = "songs")
    PostResponseDto entityToResponseDto(Post post, Long wishlistCount);

}
