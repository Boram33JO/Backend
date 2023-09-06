package com.sparta.i_mu.domain.wishlist.mapper;

import com.sparta.i_mu.domain.song.dto.SongResponseDto;
import com.sparta.i_mu.domain.song.mapper.SongMapper;
import com.sparta.i_mu.domain.wishlist.dto.WishListResponseDto;
import com.sparta.i_mu.domain.post.entity.Post;
import com.sparta.i_mu.domain.postsonglink.repository.PostSongLinkRepository;
import com.sparta.i_mu.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WishListMapper {
    private final PostSongLinkRepository postSongLinkRepository;
    private final WishlistRepository wishlistRepository;
    private final SongMapper songMapper;

    public WishListResponseDto mapToWishListResponseDto(Post post) {
        List<SongResponseDto> songs = postSongLinkRepository.findAllByPostId(post.getId())
                .stream()
                .map(postSongLink -> songMapper.entityToResponseDto(postSongLink.getSong()))
                .collect(Collectors.toList());

        return WishListResponseDto.builder()
                .postId(post.getId())
                .category(post.getCategory().getId())
                .wishlistCount(post.getWishlistCount())
                .viewCount(post.getViewCount())
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