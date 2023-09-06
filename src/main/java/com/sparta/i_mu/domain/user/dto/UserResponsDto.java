package com.sparta.i_mu.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.i_mu.domain.comment.dto.CommentListResponseDto;
import com.sparta.i_mu.domain.follow.dto.FollowListResponseDto;
import com.sparta.i_mu.domain.post.dto.PostListResponseDto;
import com.sparta.i_mu.domain.wishlist.dto.WishListResponseDto;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponsDto {

    private Boolean follow;

    private UserInfoResponseDto userInfo;

    private List<FollowListResponseDto> followList;

    private List<PostListResponseDto> postList;

    private List<WishListResponseDto> wishList;

    private List<CommentListResponseDto> commentList;

    public UserResponsDto(UserInfoResponseDto userInfo, List<PostListResponseDto> postList, List<FollowListResponseDto> followList, Boolean isFollow) {
        this.userInfo = userInfo;
        this.postList = postList;
        this.followList = followList;
        this.follow = isFollow;
    }

    public UserResponsDto(UserInfoResponseDto userInfo, List<PostListResponseDto> postList, List<FollowListResponseDto> followList, List<CommentListResponseDto> commentList, List<WishListResponseDto> wishList) {
        this.userInfo = userInfo;
        this.postList = postList;
        this.followList = followList;
        this.commentList = commentList;
        this.wishList = wishList;
    }

}
