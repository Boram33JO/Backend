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
        String getUserImage = findProfile.getUserImage();
        String getIntroduce = findProfile.getIntroduce();
        String getPassword = findProfile.getPassword();
        String getNickname = findProfile.getNickname();

        if (requestDto == null) {
            requestDto = new ProfileRequestDto();
        }

        // 닉네임 중복 확인, api 따로 빼는게 좋을듯
        boolean checkNicknameDuplicate = profileRepository.existsByNickname(requestDto.getNickname());
        if (checkNicknameDuplicate) {
            return new ResponseResource<> (false, "닉네임 중복", null);
        }

        // 새로운 방법 강구 필요
        if (requestDto.getIntroduce() != null) {
            getIntroduce = requestDto.getIntroduce();
        }

        if (requestDto.getPassword() != null) {
            getPassword = requestDto.getPassword();
        }

        if (requestDto.getNickname() != null) {
            getNickname = requestDto.getNickname();
        }

        // 기존 이미지 s3 bucket 삭제 후 업로드
        if (multipartFile != null) {
            awsS3Util.deleteImage(getUserImage);
            getUserImage = awsS3Util.uploadImage(multipartFile);
        }

        Profile profile = Profile.builder()
                .userImage(getUserImage)
                .password(passwordEncoder.encode(getPassword))
                .nickname(getNickname)
                .introduce(getIntroduce)
                .build();

        findProfile.update(profile);

        return new ResponseResource<> (true, "프로필 수정 성공", null);

    }
}
