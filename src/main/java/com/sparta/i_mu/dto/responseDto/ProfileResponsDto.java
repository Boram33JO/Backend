package com.sparta.i_mu.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileResponsDto {
    private String nickname;

    private List<FollowListResponseDto> followList;

    private List<PostListResponseDto> postList;

    private List<WishlistResponseDto> wishList;

    private List<CommentListResponseDto> commentList;

    public ProfileResponsDto(String nickname, List<PostListResponseDto> postList, List<FollowListResponseDto> followList) {
        this.nickname = nickname;
        this.postList = postList;
        this.followList = followList;
    }

    public ProfileResponsDto(String nickname, List<PostListResponseDto> postList, List<FollowListResponseDto> followList, List<CommentListResponseDto> commentList, List<WishlistResponseDto> wishList) {
        this.nickname = nickname;
        this.postList = postList;
        this.followList = followList;
        this.commentList = commentList;
        this.wishList = wishList;
    }

}
