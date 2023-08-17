package com.sparta.i_mu.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.i_mu.dto.KakaoResult;
import com.sparta.i_mu.dto.KakaoUserResponseDto;
import com.sparta.i_mu.dto.requestDto.NicknameRequestDto;
import com.sparta.i_mu.dto.requestDto.PasswordRequestDto;
import com.sparta.i_mu.dto.requestDto.SignUpRequestDto;
import com.sparta.i_mu.dto.requestDto.UserRequestDto;
import com.sparta.i_mu.dto.responseDto.*;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.util.JwtUtil;
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
    private final JwtUtil jwtUtil;

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
    public GetFollowResponseDto getUserFollow(@PathVariable Long userId) {
        return userService.getUserFollow(userId);
    }

    @GetMapping("/profile/{userId}/posts")
    public GetPostResponseDto getUserPosts(@PathVariable Long userId) {
        return userService.getUserPosts(userId);
    }

    @GetMapping("/profile/{userId}/comments")
    public List<CommentListResponseDto> getUserComments(@PathVariable Long userId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.getUserComments(userId, Optional.ofNullable(userDetails));
    }

    @GetMapping("/profile/{userId}/wishlist")
    public List<WishListResponseDto> getUserWishlist(@PathVariable Long userId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.getUserWishlist(userId, Optional.ofNullable(userDetails));
    }

    @PostMapping("/profile/check")
    public ResponseResource<?> checkNickname(@RequestBody @Valid NicknameRequestDto requestDto) {
        return userService.checkNickname(requestDto);
    }


    // 카카오 로그인
    @PostMapping ("/oauth/token")
    public ResponseEntity<KakaoUserResponseDto> kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        KakaoResult kakaoResult = kakaoService.kakaoLogin(code);
        HttpHeaders headers = new HttpHeaders();
        headers.add(jwtUtil.HEADER_ACCESS_TOKEN,  kakaoResult.getAccessToken()); // accessToken 토큰을 헤더에 추가
        headers.add(jwtUtil.HEADER_REFRESH_TOKEN,  kakaoResult.getRefreshToken()); // accessToken 토큰을 헤더에 추가
        return new ResponseEntity<>(kakaoResult.getUserInfo(), headers, HttpStatus.OK);
    }
}