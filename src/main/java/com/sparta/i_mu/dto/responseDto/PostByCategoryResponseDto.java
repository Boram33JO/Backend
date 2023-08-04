package com.sparta.i_mu.dto.responseDto;

import com.sparta.i_mu.entity.Category;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PostByCategoryResponseDto {
    private String category;
    private List<PostResponseDto> postByCategoryResponseDtoList;
    private Long totalCount;
}
