package com.sparta.i_mu.mapper;

import com.sparta.i_mu.dto.responseDto.SongResponseDto;
import com.sparta.i_mu.dto.responseDto.WishListResponseDto;
import com.sparta.i_mu.entity.Post;
import com.sparta.i_mu.repository.PostSongLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WishListMapper {
    private final PostSongLinkRepository postSongLinkRepository;
    private final SongMapper songMapper;

    public WishListResponseDto mapToWishListResponseDto(Post post) {
        List<SongResponseDto> songs = postSongLinkRepository.findAllByPostId(post.getId())
                .stream()
                .map(postSongLink -> songMapper.entityToResponseDto(postSongLink.getSong()))
                .collect(Collectors.toList());

        return WishListResponseDto.builder()
                .postId(post.getId())
                .postTitle(post.getPostTitle())
                .createdAt(post.getCreatedAt())
                .content(post.getContent())
                .userId(post.getUser().getId())
                .userImage(post.getUser().getUserImage())
                .nickname(post.getUser().getNickname())
                .songs(songs)
                .build();
    }

}