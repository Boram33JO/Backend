package com.sparta.i_mu.domain.wishlist.service;

import com.sparta.i_mu.domain.notification.service.NotificationService;
import com.sparta.i_mu.domain.post.entity.Post;
import com.sparta.i_mu.domain.user.entity.User;
import com.sparta.i_mu.domain.wishlist.entity.Wishlist;
import com.sparta.i_mu.global.errorCode.ErrorCode;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.domain.post.repository.PostRepository;
import com.sparta.i_mu.domain.wishlist.repository.WishlistRepository;
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
    private final NotificationService notificationService;

    @Transactional
    public ResponseResource<?> createWishlist(Long postId, User user) {
        Post post = postRepository.findByIdAndDeletedFalseForUpdate(postId).orElseThrow(() -> new IllegalArgumentException(ErrorCode.POST_NOT_EXIST.getMessage()));

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
        notificationService.wishlistSend(post.getUser(), user, postId, post.getPostTitle(), "wishlist");

        return ResponseResource.message("좋아요 성공", HttpStatus.OK);
    }

}
