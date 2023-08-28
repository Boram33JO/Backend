package com.sparta.i_mu.dto.responseDto;

import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class GetWishListResponseDto {
    private String nickname;
    private Page<WishListResponseDto> wishList;

    public GetWishListResponseDto(String nickname, Page<WishListResponseDto> wishListResponseDtoList) {
        this.nickname = nickname;
        this.wishList = wishListResponseDtoList;
    }
}
