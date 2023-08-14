package com.sparta.i_mu.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.i_mu.dto.KakaoResult;
import com.sparta.i_mu.dto.KakaoUserInfo;
import com.sparta.i_mu.dto.requestDto.PasswordRequestDto;
import com.sparta.i_mu.dto.requestDto.SignUpRequestDto;
import com.sparta.i_mu.dto.requestDto.UserRequestDto;
import com.sparta.i_mu.dto.responseDto.*;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.security.UserDetailsImpl;
import com.sparta.i_mu.service.KakaoService;
import com.sparta.i_mu.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final KakaoService kakaoService;

    @PostMapping("/user/signup")
    public ResponseEntity<MessageResponseDto> createUser(@RequestBody @Valid SignUpRequestDto signUpRequestDto) {
        return userService.createUser(signUpRequestDto);
    }

    @GetMapping("/profile/{userId}")
    public UserResponsDto getUser(@PathVariable Long userId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.getUser(userId, Optional.ofNullable(userDetails));
    }

    @PutMapping("/profile/{userId}")
    public ResponseResource<?> updateUser(@PathVariable Long userId, @RequestPart(value = "userImage", required = false) MultipartFile multipartFile, @RequestPart(required = false) @Valid UserRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.updateUser(userId, multipartFile, requestDto, userDetails.getUser().getId());
    }

    @PutMapping("/profile/{userId}/password")
    public ResponseResource<?> updatePassword(@PathVariable Long userId, @RequestBody @Valid PasswordRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.updatePassword(userId, requestDto, userDetails.getUser());
    }

    @GetMapping("/profile/{userId}/follow")
    public List<FollowListResponseDto> getUserFollow(@PathVariable Long userId) {
        return userService.getUserFollow(userId);
    }

    @GetMapping("/profile/{userId}/posts")
    public List<PostListResponseDto> getUserPosts(@PathVariable Long userId) {
        return userService.getUserPosts(userId);
    }

    @GetMapping("/profile/{userId}/comments")
    public List<CommentListResponseDto> getUserComments(@PathVariable Long userId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.getUserComments(userId, Optional.ofNullable(userDetails));
    }

    @GetMapping("/profile/{userId}/wishlist")
    public List<PostListResponseDto> getUserWishlist(@PathVariable Long userId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.getUserWishlist(userId, Optional.ofNullable(userDetails));
    }


    // 카카오 로그인
    @PostMapping ("/oauth/token")
    public ResponseEntity<KakaoUserInfo> kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        KakaoResult kakaoResult = kakaoService.kakaoLogin(code);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization",  kakaoResult.getToken()); // 토큰을 헤더에 추가
        return new ResponseEntity<>(kakaoResult.getUserInfo(), headers, HttpStatus.OK);
    }
}