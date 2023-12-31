package com.sparta.i_mu.domain.post.mapper;

import com.sparta.i_mu.domain.post.dto.PostListResponseDto;
import com.sparta.i_mu.domain.post.dto.PostResponseDto;
import com.sparta.i_mu.domain.song.dto.SongResponseDto;
import com.sparta.i_mu.domain.post.entity.Post;
import com.sparta.i_mu.domain.comment.repository.CommentRepository;
import com.sparta.i_mu.domain.follow.repository.FollowReporitory;
import com.sparta.i_mu.domain.postsonglink.repository.PostSongLinkRepository;
import com.sparta.i_mu.domain.wishlist.repository.WishlistRepository;
import com.sparta.i_mu.global.security.UserDetailsImpl;
import com.sparta.i_mu.domain.song.mapper.SongMapper;
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
                .wishlistCount(post.getWishlistCount())
                .viewCount(post.getViewCount())
                .songs(songs)
                .location(post.getLocation())
                .build();
    }

    // 상세 게시물 조회
    public PostResponseDto mapToPostResponseDto(Post post, Optional<UserDetailsImpl> userDetails) {
        boolean isWishlist = userDetails.isPresent() && wishlistRepository.existsByPostIdAndUserId(post.getId(), userDetails.get().getUser().getId());
        boolean isfollow = userDetails.isPresent() && followReporitory.existsByFollowUserIdAndFollowedUserId(post.getUser().getId(), userDetails.get().getUser().getId());

//        List<CommentResponseDto> comments = commentRepository.findAllByPostIdAndDeletedFalse(post.getId())
//                .stream()
//                .map(CommentResponseDto::new)
//                .toList();

        List<SongResponseDto> songs = postSongLinkRepository.findAllByPostId(post.getId())
                .stream()
                .map(postSongLink -> songMapper.entityToResponseDto(postSongLink.getSong())) // SongResponseDto로의 매핑 로직이 필요합니다
                .collect(Collectors.toList());

        return PostResponseDto.builder()
                .userId(post.getUser().getId())
                .postTitle(post.getPostTitle())
                .postId(post.getId())
                .viewCount(post.getViewCount())
                .nickname(post.getUser().getNickname())
                .userImage(post.getUser().getUserImage())
                .content(post.getContent())
                .category(post.getCategory().getId())
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .wishlist(isWishlist)
                .follow(isfollow)
                .wishlistCount(post.getWishlistCount())
                .songs(songs)
                .location(post.getLocation())
                .build();
    }

    // 작성자가 작성한 게시글 조회 + 좋아요한 리스트 조회
    public PostListResponseDto mapToPostListResponseDto(Post post) {
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
                .wishlistCount(post.getWishlistCount())
                .viewCount(post.getViewCount())
                .songs(songs)
                .build();
    }

}