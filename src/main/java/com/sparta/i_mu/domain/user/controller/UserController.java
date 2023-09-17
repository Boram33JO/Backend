package com.sparta.i_mu.domain.user.controller;

import com.sparta.i_mu.domain.comment.dto.CommentListResponseDto;
import com.sparta.i_mu.domain.user.dto.*;
import com.sparta.i_mu.domain.wishlist.dto.WishListResponseDto;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.security.UserDetailsImpl;
import com.sparta.i_mu.domain.user.service.UserService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/user")
@Tag(name = "User", description = "유저 API Document")
public class UserController {

    private final UserService userService;


    //로그아웃
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃")
    public ResponseResource<?> logout(@RequestHeader("AccessToken") String accessToken){
        log.info("로그아웃 메서드 진입");
        return userService.logout(accessToken);
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원가입")
    public ResponseEntity<MessageResponseDto> createUser(@RequestBody @Valid SignUpRequestDto signUpRequestDto) {
        return userService.createUser(signUpRequestDto);
    }

    @PostMapping("/cancel")
    @Operation(summary = "회원탈퇴", description = "회원탈퇴")
    public ResponseResource<?> cancelUser(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                          HttpServletRequest req) {
        return userService.cancelUser(userDetails.getUser(), req);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "프로필 조회", description = "상대방 프로필 조회는 팔로우, 작성글 조회, 본인 프로필 조회는 팔로우, 작성글, 좋아요, 댓글 조회")
    @Parameter(name = "userId", description = "조회할 유저의 ID ")
    public UserResponsDto getUser(@PathVariable Long userId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.getUser(userId, Optional.ofNullable(userDetails));
    }

    @PutMapping("/{userId}")
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

    @PutMapping("/{userId}/password")
    @Operation(summary = "비밀번호 수정", description = "비밀번호 수정")
    @Parameter(name = "userId", description = "비밀번호 수정할 유저의 ID ")
    public ResponseResource<?> updatePassword(@PathVariable Long userId, @RequestBody @Valid PasswordRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userService.updatePassword(userId, requestDto, userDetails.getUser());
    }

    @GetMapping("/{userId}/follow")
    @Operation(summary = "팔로우 조회", description = "팔로우 조회")
    @Parameter(name = "userId", description = "팔로우 조회할 유저의 ID ")
    public GetFollowResponseDto getUserFollow(@PathVariable Long userId,
                                              Pageable pageable) {
        return userService.getUserFollow(userId, pageable);
    }

    @GetMapping("/{userId}/posts")
    @Operation(summary = "작성글 조회", description = "작성글 조회")
    @Parameter(name = "userId", description = "작성글 조회할 유저의 ID ")
    public GetPostResponseDto getUserPosts(@PathVariable Long userId,
                                           Pageable pageable) {
        return userService.getUserPosts(userId, pageable);
    }

    @GetMapping("/{userId}/comments")
    @Operation(summary = "댓글 조회", description = "댓글 조회")
    @Parameter(name = "userId", description = "댓글 조회할 유저의 ID ")
    public Page<CommentListResponseDto> getUserComments(@PathVariable Long userId,
                                                        @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                        Pageable pageable) {
        return userService.getUserComments(userId, Optional.ofNullable(userDetails), pageable);
    }

    @GetMapping("/{userId}/wishlist")
    @Operation(summary = "좋아요 조회", description = "좋아요 조회")
    @Parameter(name = "userId", description = "좋아요 조회할 유저의 ID ")
    public Page<WishListResponseDto> getUserWishlist(@PathVariable Long userId,
                                                     @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                     Pageable pageable) {
        return userService.getUserWishlist(userId, Optional.ofNullable(userDetails), pageable);
    }

    @PostMapping("/check")
    @Operation(summary = "닉네임 중복 체크", description = "닉네임 중복 체크")
    public ResponseResource<?> checkNickname(@RequestBody @Valid NicknameRequestDto requestDto) {
        return userService.checkNickname(requestDto);
    }

}