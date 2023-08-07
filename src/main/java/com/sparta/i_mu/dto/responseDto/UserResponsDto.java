package com.sparta.i_mu.dto.responseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponsDto {
    private String nickname;

    private List<FollowListResponseDto> followList;

    private List<PostResponseDto> postList;

    private List<PostResponseDto> wishList;

    private List<CommentListResponseDto> commentList;

    public UserResponsDto(String nickname, List<PostResponseDto> postList, List<FollowListResponseDto> followList) {
        this.nickname = nickname;
        this.postList = postList;
        this.followList = followList;
    }

    public UserResponsDto(String nickname, List<PostResponseDto> postList, List<FollowListResponseDto> followList, List<CommentListResponseDto> commentList, List<PostResponseDto> wishList) {
        this.nickname = nickname;
        this.postList = postList;
        this.followList = followList;
        this.commentList = commentList;
        this.wishList = wishList;
    }

}
