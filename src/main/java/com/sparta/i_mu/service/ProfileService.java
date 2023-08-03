package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.requestDto.ProfileRequestDto;
import com.sparta.i_mu.entity.Profile;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.util.AwsS3Util;
import com.sparta.i_mu.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    private final PasswordEncoder passwordEncoder;

    private final AwsS3Util awsS3Util;

    @Transactional
    public ResponseResource<?> updateProfile(MultipartFile multipartFile, ProfileRequestDto requestDto, Long userId) {
        Profile findProfile = profileRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 유저입니다."));
        String profileImageUrl = findProfile.getProfileImageUrl();

//        닉네임 중복 확인, api 따로 빼는게 좋을듯
        boolean checkNicknameDuplicate = profileRepository.existsByNickname(requestDto.getNickname());
        if (checkNicknameDuplicate) {
            return new ResponseResource<> (false, "닉네임 중복", null);
        }

        if (multipartFile != null) {
            profileImageUrl = awsS3Util.uploadImage(multipartFile);
        }

        Profile profile = Profile.builder()
                .profileImageUrl(profileImageUrl)
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .nickname(requestDto.getNickname())
                .build();

        findProfile.update(profile);

        return new ResponseResource<> (true, "프로필 수정 성공", null);

    }
}
