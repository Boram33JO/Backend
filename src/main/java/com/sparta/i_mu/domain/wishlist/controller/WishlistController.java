package com.sparta.i_mu.domain.wishlist.controller;

import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.security.UserDetailsImpl;
import com.sparta.i_mu.domain.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "좋아요 API Document")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/posts/{postId}/wishlists")
    @Operation(summary = "게시글 좋아요", description = "게시글 좋아요")
    @Parameter(name = "postId", description = "좋아요할 게시글의 ID ")
    public ResponseResource<?> createWishlist (@PathVariable Long postId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return wishlistService.createWishlist(postId, userDetails.getUser());
    }

}
