package com.sparta.i_mu.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponsDto {

    private UserInfoResponseDto userInfo;

    private List<FollowListResponseDto> followList;

    private List<PostListResponseDto> postList;

    private List<WishListResponseDto> wishList;

    private List<CommentListResponseDto> commentList;

    public UserResponsDto(UserInfoResponseDto userInfo, List<PostListResponseDto> postList, List<FollowListResponseDto> followList) {
        this.userInfo = userInfo;
        this.postList = postList;
        this.followList = followList;
    }

    public UserResponsDto(UserInfoResponseDto userInfo, List<PostListResponseDto> postList, List<FollowListResponseDto> followList, List<CommentListResponseDto> commentList, List<WishListResponseDto> wishList) {
        this.userInfo = userInfo;
        this.postList = postList;
        this.followList = followList;
        this.commentList = commentList;
        this.wishList = wishList;
    }

}
