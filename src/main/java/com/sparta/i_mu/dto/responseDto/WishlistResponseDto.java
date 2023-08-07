package com.sparta.i_mu.dto.responseDto;

import com.sparta.i_mu.entity.Wishlist;
import lombok.Getter;

@Getter
public class WishlistResponseDto {
    private Long id;
    private String content;


    public WishlistResponseDto(Wishlist wishlist) {
        this.id = wishlist.getPost().getId();
        this.content = wishlist.getPost().getContent();
    }

}
