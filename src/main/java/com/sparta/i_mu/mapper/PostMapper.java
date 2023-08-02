package com.sparta.i_mu.mapper;

import com.sparta.i_mu.dto.responseDto.PostResponseDto;
import com.sparta.i_mu.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper(uses = {PostSongLinkMapper.class})
public interface PostMapper {
    PostMapper POST_INSTANCE = Mappers.getMapper(PostMapper.class);

    @Mapping(source = "post.user.id", target = "userId")
    @Mapping(source = "post.user.nickname", target = "nickname")
    @Mapping(source = "post.postSongLink", target = "songs") // PostSongLink 리스트를 SongResponseDto 리스트로 변환해야 함
    PostResponseDto entityToDto(Post post);

}
