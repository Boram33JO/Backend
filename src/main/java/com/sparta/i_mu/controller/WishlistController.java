package com.sparta.i_mu.controller;

import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.security.UserDetailsImpl;
import com.sparta.i_mu.service.WishlistService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Wishlist API")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/posts/{postId}/wishlist")
    public ResponseResource<?> createWishlist (@PathVariable Long postId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return wishlistService.createWishlist(postId, userDetails.getUser());
    }

}
