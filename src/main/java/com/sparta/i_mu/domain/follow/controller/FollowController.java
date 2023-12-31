package com.sparta.i_mu.domain.follow.controller;

import com.sparta.i_mu.domain.follow.dto.FollowPopularResponseDto;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.security.UserDetailsImpl;
import com.sparta.i_mu.domain.follow.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Follow", description = "팔로우 API Document")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/user/{userId}/follows")
    @Operation(summary = "유저 팔로우", description = "유저 팔로우")
    @Parameter(name = "userId", description = "팔로우할 유저 ID ")
    public ResponseResource<?> createFollow (@PathVariable Long userId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return followService.createFollow(userId, userDetails.getUser());
    }

    @GetMapping("/top-follows")
    @Operation(summary = "인기 팔로워", description = "팔로우 많은 상위 4명 조회")
    public List<FollowPopularResponseDto> getFollowPopular () {
        return followService.getFollowPopular();
    }

}
