package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.requestDto.PasswordRequestDto;
import com.sparta.i_mu.dto.requestDto.SignUpRequestDto;
import com.sparta.i_mu.dto.requestDto.UserRequestDto;
import com.sparta.i_mu.dto.responseDto.*;
import com.sparta.i_mu.entity.*;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.util.AwsS3Util;
import com.sparta.i_mu.mapper.PostMapper;
import com.sparta.i_mu.repository.*;
import com.sparta.i_mu.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor //의존성 주입
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final PostRepository postRepository;

    private final WishlistRepository wishlistRepository;

    private final FollowReporitory followReporitory;

    private final CommentRepository commentRepository;

    private final AwsS3Util awsS3Util;

    private final PostMapper postMapper;

    // 회원가입 서비스
    public ResponseEntity<MessageResponseDto> createUser(SignUpRequestDto signUpRequestDto){
        String nickname = signUpRequestDto.getNickname();
        String password = passwordEncoder.encode(signUpRequestDto.getPassword());
        String email = signUpRequestDto.getEmail();

        // 회원 email 중복 확인
        Optional<User> checkEmail = userRepository.findByEmail(email);
        if (checkEmail.isPresent()) {
            throw new IllegalArgumentException("중복된 email 입니다.");
        }

        // 회원 nickname 중복 확인
        Optional<User> checkNickname = userRepository.findByNickname(nickname);
        if (checkNickname.isPresent()) {
            throw new IllegalArgumentException("중복된 nickname 입니다.");
        }

        // 사용자 등록

        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password(password)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponseDto("회원가입 되었습니다.", HttpStatus.OK.toString()));
    }

    public UserResponsDto getUser(Long userId, Optional<UserDetailsImpl> userDetails) {
        // profile, user 합치고 연관관계 설정 후 정보 조회부분 리팩토링 필요
        // response 수정 필요

        User user = userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 유저 입니다"));
        String nickname = user.getNickname();
        String introduce = user.getIntroduce();

        List<FollowListResponseDto> followResponseDtoList = getFollowListResponseDtoList(userId);
        List<PostListResponseDto> postResponseDtoList = getPostListResponseDtoList(userId);

        if (userDetails.isPresent() && userDetails.get().getUser().getId().equals(userId)) {
            List<PostListResponseDto> wishlistResponseDtoList = getWishlistResponseDtoList(userId);
            List<CommentListResponseDto> commentResponseDtoList = getCommentListResponseDtoList(userId);

            UserResponsDto responsDto = new UserResponsDto(nickname, introduce, postResponseDtoList, followResponseDtoList, commentResponseDtoList, wishlistResponseDtoList);

            return responsDto;
        }

        UserResponsDto responsDto = new UserResponsDto(nickname, introduce, postResponseDtoList, followResponseDtoList);

        return responsDto;
    }

    @Transactional
    public ResponseResource<?> updateUser(Long id, MultipartFile multipartFile, UserRequestDto requestDto, Long userId) {
        if (!userId.equals(id)) {
            throw new IllegalArgumentException("로그인한 유저가 아닙니다.");
        }
        User findUser = userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 유저입니다."));

        String getUserImage = findUser.getUserImage();
        String getIntroduce = findUser.getIntroduce();
        String getNickname = findUser.getNickname();

        if (requestDto == null) {
            requestDto = new UserRequestDto();
        }

        // 닉네임 중복 확인, api 따로 빼는게 좋을듯
        boolean checkNicknameDuplicate = userRepository.existsByNickname(requestDto.getNickname());
        if (checkNicknameDuplicate) {
            return new ResponseResource<> (false, "닉네임 중복", null);
        }

        // 새로운 방법 강구 필요
        if (requestDto.getIntroduce() != null) {
            getIntroduce = requestDto.getIntroduce();
        }

        if (requestDto.getNickname() != null) {
            getNickname = requestDto.getNickname();
        }

        // 기존 이미지 s3 bucket 삭제 후 업로드
        if (multipartFile != null) {
            awsS3Util.deleteImage(getUserImage);
            getUserImage = awsS3Util.uploadImage(multipartFile);
        }

        User user = User.builder()
                .userImage(getUserImage)
                .nickname(getNickname)
                .introduce(getIntroduce)
                .build();

        findUser.update(user);

        return new ResponseResource<> (true, "프로필 수정 성공", null);
    }

    @Transactional
    public ResponseResource<?> updatePassword(Long userId, PasswordRequestDto requestDto, User user) {
        String changePassword = requestDto.getChangePassword();

        User findUser = userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 유저입니다."));
        if (!userId.equals(user.getId())) {
            throw new IllegalArgumentException("로그인한 유저가 아닙니다.");
        }

        if (!passwordEncoder.matches(requestDto.getOriginPassword(), user.getPassword())){
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }

        User changePassswordUser = User.builder()
                .password(passwordEncoder.encode(changePassword))
                .build();

        findUser.passwordUpdate(changePassswordUser);

        return new ResponseResource<> (true, "비밀번호 수정 성공", null);

    }

    private List<FollowListResponseDto> getFollowListResponseDtoList(Long userId) {
        List<Follow> followList = followReporitory.findAllByFollowedUserId(userId);
        List<FollowListResponseDto> followResponseDtoList = followList.stream()
                .map(FollowListResponseDto::new)
                .toList();
        return followResponseDtoList;
    }

    private List<PostListResponseDto> getPostListResponseDtoList(Long userId) {
        List<Post> postList = postRepository.findAllByUserId(userId);
        List<PostListResponseDto> postResponseDtoList = postList.stream()
                .map(postMapper::mapToPostListResponseDto)
                .collect(Collectors.toList());

        return postResponseDtoList;
    }

    private List<PostListResponseDto> getWishlistResponseDtoList(Long userId) {
        List<Wishlist> wishList = wishlistRepository.findAllByUserId(userId);
        List<PostListResponseDto> wishListReponsePostList = wishList.stream()
                .map(wishlist -> postMapper.mapToPostListResponseDto(wishlist.getPost()))
                .collect(Collectors.toList());

        return wishListReponsePostList;
    }

    private List<CommentListResponseDto> getCommentListResponseDtoList(Long userId) {
        List<Comment> commentList = commentRepository.findAllByUserId(userId);
        List<CommentListResponseDto> commentResponseDtoList = commentList.stream()
                .map(CommentListResponseDto::new)
                .toList();
        return commentResponseDtoList;
    }
}