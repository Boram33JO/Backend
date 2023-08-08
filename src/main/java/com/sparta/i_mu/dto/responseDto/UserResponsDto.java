package com.sparta.i_mu.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponsDto {
    private String nickname;

    private String introduce;

    private List<FollowListResponseDto> followList;

    private List<PostListResponseDto> postList;

    private List<PostListResponseDto> wishList;

    private List<CommentListResponseDto> commentList;

    public UserResponsDto(String nickname, String introduce, List<PostListResponseDto> postList, List<FollowListResponseDto> followList) {
        this.nickname = nickname;
        this.introduce = introduce;
        this.postList = postList;
        this.followList = followList;
    }

    public UserResponsDto(String nickname, String introduce, List<PostListResponseDto> postList, List<FollowListResponseDto> followList, List<CommentListResponseDto> commentList, List<PostListResponseDto> wishList) {
        this.nickname = nickname;
        this.introduce = introduce;
        this.postList = postList;
        this.followList = followList;
        this.commentList = commentList;
        this.wishList = wishList;
    }

}
