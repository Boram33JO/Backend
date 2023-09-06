package com.sparta.i_mu.domain.post.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PostByCategoryResponseDto {
    private Long category;
    private List<PostResponseDto> postByCategoryResponseDtoList;
}
