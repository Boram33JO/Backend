package com.sparta.i_mu.controller;

import com.sparta.i_mu.dto.requestDto.ProfileRequestDto;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    @PutMapping
    public ResponseResource<?> updateProfile(@RequestPart(value = "userImage", required = false) MultipartFile multipartFile, @RequestPart(required = false) ProfileRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return profileService.updateProfile(multipartFile, requestDto, userDetails.getUser().getId());
    }

}
