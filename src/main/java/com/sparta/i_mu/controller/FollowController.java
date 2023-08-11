package com.sparta.i_mu.controller;

import com.sparta.i_mu.dto.responseDto.FollowPopularResponseDto;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.security.UserDetailsImpl;
import com.sparta.i_mu.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/profile/{userId}/follow")
    public ResponseResource<?> createFollow (@PathVariable Long userId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return followService.createFollow(userId, userDetails.getUser());
    }

    @GetMapping("/popular")
    public List<FollowPopularResponseDto> createFollow () {
        return followService.getFollowPopular();
    }

//    @GetMapping("/profile/{userId}/follow")
//    public List<FollowResponseDto> findFollow (@PathVariable Long userId) {
//        return followService.findFollow(userId);
//    }

}
