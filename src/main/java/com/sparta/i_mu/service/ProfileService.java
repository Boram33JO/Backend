package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.requestDto.ProfileRequestDto;
import com.sparta.i_mu.dto.responseDto.*;
import com.sparta.i_mu.entity.Follow;
import com.sparta.i_mu.entity.Post;
import com.sparta.i_mu.entity.Profile;
import com.sparta.i_mu.entity.Wishlist;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.util.AwsS3Util;
import com.sparta.i_mu.repository.PostRepository;
import com.sparta.i_mu.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    private final ProfileRepository profileRepository;

    private final PostRepository postRepository;

    private final WishlistRepository wishlistRepository;

    private final FollowReporitory followReporitory;

    private final CommentRepository commentRepository;

    private final PasswordEncoder passwordEncoder;

    private final AwsS3Util awsS3Util;

    public ProfileResponsDto getProfile(Long userId, Optional<UserDetailsImpl> userDetails) {
        // profile, user 합치고 연관관계 설정 후 정보 조회부분 리팩토링 필요
        // response 수정 필요

        User user = userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 유저 입니다"));
        String nickname = user.getNickname();

        List<FollowListResponseDto> followResponseDtoList = getFollowListResponseDtoList(userId);
        List<PostListResponseDto> postResponseDtoList = getPostListResponseDtoList(userId);

        if (userDetails.isPresent() && userDetails.get().getUser().getId().equals(userId)) {
            List<WishlistResponseDto> wishlistResponseDtoList = getWishlistResponseDtoList(userId);
            List<CommentListResponseDto> commentResponseDtoList = getCommentListResponseDtoList(userId);

            ProfileResponsDto responsDto = new ProfileResponsDto(nickname, postResponseDtoList, followResponseDtoList, commentResponseDtoList, wishlistResponseDtoList);

            return responsDto;
        }

        ProfileResponsDto responsDto = new ProfileResponsDto(nickname, postResponseDtoList, followResponseDtoList);

        return responsDto;
    }

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

    private List<CommentListResponseDto> getCommentListResponseDtoList(Long userId) {
        List<Comment> commentList = commentRepository.findAllByUserId(userId);
        List<CommentListResponseDto> commentResponseDtoList = commentList.stream()
                .map(CommentListResponseDto::new)
                .toList();
        return commentResponseDtoList;
    }

    private List<WishlistResponseDto> getWishlistResponseDtoList(Long userId) {
        List<Wishlist> wishList = wishlistRepository.findAllByUserId(userId);
        List<WishlistResponseDto> wishlistResponseDtoList = wishList.stream()
                .map(WishlistResponseDto::new)
                .toList();
        return wishlistResponseDtoList;
    }

    private List<PostListResponseDto> getPostListResponseDtoList(Long userId) {
        List<Post> postList = postRepository.findAllByUserId(userId);
        List<PostListResponseDto> postResponseDtoList = postList.stream()
                .map(PostListResponseDto::new)
                .toList();
        return postResponseDtoList;
    }

    private List<FollowListResponseDto> getFollowListResponseDtoList(Long userId) {
        List<Follow> followList = followReporitory.findAllByFollowedUserId(userId);
        List<FollowListResponseDto> followResponseDtoList = followList.stream()
                .map(FollowListResponseDto::new)
                .toList();
        return followResponseDtoList;
    }

}
