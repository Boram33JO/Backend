package com.sparta.i_mu.mapper;

import com.sparta.i_mu.dto.responseDto.*;
import com.sparta.i_mu.entity.Post;
import com.sparta.i_mu.entity.User;
import com.sparta.i_mu.repository.CommentRepository;
import com.sparta.i_mu.repository.FollowReporitory;
import com.sparta.i_mu.repository.PostSongLinkRepository;
import com.sparta.i_mu.repository.WishlistRepository;
import com.sparta.i_mu.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostMapper {
    private final WishlistRepository wishlistRepository;
    private final FollowReporitory followReporitory;
    private final CommentRepository commentRepository;
    private final PostSongLinkRepository postSongLinkRepository;
    private final SongMapper songMapper;

    //기본 Post responseDto -> 댓글/좋아요/팔로우를 제외한
    public PostResponseDto mapToPostResponseDto(Post post) {
        Long wishlistCount = wishlistRepository.countByPostId(post.getId());
        List<SongResponseDto> songs = postSongLinkRepository.findAllByPostId(post.getId())
                .stream()
                .map(postSongLink -> songMapper.entityToResponseDto(postSongLink.getSong())) // SongResponseDto로의 매핑 로직이 필요합니다
                .collect(Collectors.toList());

        return PostResponseDto.builder()
                .userId(post.getUser().getId())
                .postId(post.getId())
                .viewCount(post.getViewCount())
                .userImage(post.getUser().getUserImage())
                .postTitle(post.getPostTitle())
                .nickname(post.getUser().getNickname())
                .content(post.getContent())
                .category(post.getCategory().getId())
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .deletedAt(post.getDeleteAt())
                .deleted(post.getDeleted())
                .wishlistCount(wishlistCount)
                .songs(songs)
                .location(post.getLocation())
                .build();
    }

    // 상세 게시물 조회
    public PostResponseDto mapToPostResponseDto(Post post, Optional<UserDetailsImpl> userDetails) {
        boolean isWishlist = userDetails.isPresent() && wishlistRepository.existsByPostIdAndUserId(post.getId(), userDetails.get().getUser().getId());
        boolean isfollow = userDetails.isPresent() && followReporitory.existsByFollowUserIdAndFollowedUserId(post.getUser().getId(), userDetails.get().getUser().getId());

        Long wishlistCount = wishlistRepository.countByPostId(post.getId());

        List<CommentResponseDto> comments = commentRepository.findAllByPostIdAndDeletedFalse(post.getId())
                .stream()
                .map(CommentResponseDto::new)
                .toList();

        List<SongResponseDto> songs = postSongLinkRepository.findAllByPostId(post.getId())
                .stream()
                .map(postSongLink -> songMapper.entityToResponseDto(postSongLink.getSong())) // SongResponseDto로의 매핑 로직이 필요합니다
                .collect(Collectors.toList());

        return PostResponseDto.builder()
                .userId(post.getUser().getId())
                .postTitle(post.getPostTitle())
                .viewCount(post.getViewCount())
                .nickname(post.getUser().getNickname())
                .userImage(post.getUser().getUserImage())
                .content(post.getContent())
                .category(post.getCategory().getId())
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .wishlist(isWishlist)
                .follow(isfollow)
                .wishlistCount(wishlistCount)
                .comments(comments)
                .songs(songs)
                .location(post.getLocation())
                .build();
    }

    // 작성자가 작성한 게시글 조회 + 좋아요한 리스트 조회
    public PostListResponseDto mapToPostListResponseDto(Post post) {
        Long wishlistCount = wishlistRepository.countByPostId(post.getId());
        List<SongResponseDto> songs = postSongLinkRepository.findAllByPostId(post.getId())
                .stream()
                .map(postSongLink -> songMapper.entityToResponseDto(postSongLink.getSong()))
                .collect(Collectors.toList());

        return PostListResponseDto.builder()
                .postId(post.getId())
                .postTitle(post.getPostTitle())
                .createdAt(post.getCreatedAt())
                .content(post.getContent())
                .category(post.getCategory().getId())
                .wishlistCount(wishlistCount)
                .songs(songs)
                .build();
    }

}