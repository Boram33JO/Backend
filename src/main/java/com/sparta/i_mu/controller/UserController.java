package com.sparta.i_mu.controller;

import com.sparta.i_mu.dto.requestDto.PasswordRequestDto;
import com.sparta.i_mu.dto.requestDto.UserRequestDto;
import com.sparta.i_mu.dto.requestDto.SignUpRequestDto;
import com.sparta.i_mu.dto.responseDto.MessageResponseDto;
import com.sparta.i_mu.dto.responseDto.UserResponsDto;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.security.UserDetailsImpl;
import com.sparta.i_mu.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

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

}