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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@Slf4j
@RequestMapping("/api")
@Tag(name = "User", description = "유저 API Document")
public class UserController {
    private final UserService userService;
    private final KakaoService kakaoService;
    private final JwtUtil jwtUtil;

    //로그아웃
    @PostMapping("/user/logout")
    @Operation(summary = "로그아웃", description = "로그아웃")
    public ResponseResource<?> logout(@RequestHeader("AccessToken") String accessToken){
        log.info("로그아웃 메서드 진입");
        return userService.logout(accessToken);
    }
    @PostMapping("/user/signup")
    @Operation(summary = "회원가입", description = "회원가입")
    public ResponseEntity<MessageResponseDto> createUser(@RequestBody @Valid SignUpRequestDto signUpRequestDto) {
        return userService.createUser(signUpRequestDto);
    }

    @GetMapping("/profile/{userId}")
    @Operation(summary = "프로필 조회", description = "상대방 프로필 조회는 팔로우, 작성글 조회, 본인 프로필 조회는 팔로우, 작성글, 좋아요, 댓글 조회")
    @Parameter(name = "userId", description = "조회할 유저의 ID ")
    public UserResponsDto getUser(@PathVariable Long userId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.getUser(userId, Optional.ofNullable(userDetails));
    }

    @PutMapping("/profile/{userId}")
    @Operation(summary = "프로필 수정", description = "프로필 수정")
    @Parameter(name = "userId", description = "수정할 유저의 ID ")
    public ResponseResource<?> updateUser(@PathVariable Long userId,
                                          @RequestPart(value = "userImage", required = false) MultipartFile multipartFile,
                                          @RequestPart(required = false) @Valid UserRequestDto requestDto,
                                          @AuthenticationPrincipal UserDetailsImpl userDetails,
                                          HttpServletResponse response,
                                          HttpServletRequest request) {
        return userService.updateUser(userId, multipartFile, requestDto, userDetails.getUser().getId(),response, request);
    }

    @PutMapping("/profile/{userId}/password")
    @Operation(summary = "비밀번호 수정", description = "비밀번호 수정")
    @Parameter(name = "userId", description = "비밀번호 수정할 유저의 ID ")
    public ResponseResource<?> updatePassword(@PathVariable Long userId, @RequestBody @Valid PasswordRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.updatePassword(userId, requestDto, userDetails.getUser());
    }

    @GetMapping("/profile/{userId}/follow")
    @Operation(summary = "팔로우 조회", description = "팔로우 조회")
    @Parameter(name = "userId", description = "팔로우 조회할 유저의 ID ")
    public GetFollowResponseDto getUserFollow(@PathVariable Long userId,
                                              Pageable pageable) {
        return userService.getUserFollow(userId, pageable);
    }

    @GetMapping("/profile/{userId}/posts")
    @Operation(summary = "작성글 조회", description = "작성글 조회")
    @Parameter(name = "userId", description = "작성글 조회할 유저의 ID ")
    public GetPostResponseDto getUserPosts(@PathVariable Long userId,
                                           Pageable pageable) {
        return userService.getUserPosts(userId, pageable);
    }

    @GetMapping("/profile/{userId}/comments")
    @Operation(summary = "댓글 조회", description = "댓글 조회")
    @Parameter(name = "userId", description = "댓글 조회할 유저의 ID ")
    public Page<CommentListResponseDto> getUserComments(@PathVariable Long userId,
                                                        @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                        Pageable pageable) {
        return userService.getUserComments(userId, Optional.ofNullable(userDetails), pageable);
    }

    @GetMapping("/profile/{userId}/wishlist")
    @Operation(summary = "좋아요 조회", description = "좋아요 조회")
    @Parameter(name = "userId", description = "좋아요 조회할 유저의 ID ")
    public List<WishListResponseDto> getUserWishlist(@PathVariable Long userId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.getUserWishlist(userId, Optional.ofNullable(userDetails));
    }

    @PostMapping("/profile/check")
    @Operation(summary = "닉네임 중복 체크", description = "닉네임 중복 체크")
    public ResponseResource<?> checkNickname(@RequestBody @Valid NicknameRequestDto requestDto) {
        return userService.checkNickname(requestDto);
    }


    // 카카오 로그인
    @PostMapping ("/oauth/token")
    @Operation(summary = "카카오 로그인", description = "카카오 로그인")
    public ResponseEntity<KakaoUserResponseDto> kakaoLogin(@RequestParam String code) throws JsonProcessingException {
        KakaoResult kakaoResult = kakaoService.kakaoLogin(code);
        HttpHeaders headers = new HttpHeaders();
        headers.add(jwtUtil.HEADER_ACCESS_TOKEN,  kakaoResult.getAccessToken()); // accessToken 토큰을 헤더에 추가
        headers.add(jwtUtil.HEADER_REFRESH_TOKEN,  kakaoResult.getRefreshToken()); // accessToken 토큰을 헤더에 추가
        return new ResponseEntity<>(kakaoResult.getUserInfo(), headers, HttpStatus.OK);
    }
}