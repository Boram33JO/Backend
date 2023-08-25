package com.sparta.i_mu.service;

import com.sparta.i_mu.entity.Post;
import com.sparta.i_mu.entity.User;
import com.sparta.i_mu.entity.Wishlist;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.repository.PostRepository;
import com.sparta.i_mu.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final PostRepository postRepository;

    @Transactional
    public ResponseResource<?> createWishlist(Long postId, User user) {
        Post post = postRepository.findByIdAndDeletedFalseForUpdate(postId).orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다."));

        Optional<Wishlist> wishlist = wishlistRepository.findByPostIdAndUserId(post.getId(), user.getId());

        if (wishlist.isPresent()) {
            post.downWishlistCount();
            wishlistRepository.delete(wishlist.get());
            postRepository.save(post);
            return ResponseResource.message("좋아요 삭제", HttpStatus.OK);
        }

        post.upWishlistCount();
        Wishlist saveWishlist = Wishlist.builder()
                .post(post)
                .user(user)
                .build();

        wishlistRepository.save(saveWishlist);

        return ResponseResource.message("좋아요 성공", HttpStatus.OK);
    }

}
