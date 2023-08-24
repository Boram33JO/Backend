package com.sparta.i_mu.service;

import com.sparta.i_mu.config.RedisConfig;
import com.sparta.i_mu.dto.requestDto.NicknameRequestDto;
import com.sparta.i_mu.dto.requestDto.PasswordRequestDto;
import com.sparta.i_mu.dto.requestDto.SignUpRequestDto;
import com.sparta.i_mu.dto.requestDto.UserRequestDto;
import com.sparta.i_mu.dto.responseDto.*;
import com.sparta.i_mu.entity.*;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.util.AwsS3Util;
import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.global.util.RedisUtil;
import com.sparta.i_mu.mapper.PostMapper;
import com.sparta.i_mu.mapper.WishListMapper;
import com.sparta.i_mu.repository.*;
import com.sparta.i_mu.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor //의존성 주입
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final PostRepository postRepository;

    private final WishlistRepository wishlistRepository;

    private final FollowReporitory followReporitory;

    private final CommentRepository commentRepository;

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final AwsS3Util awsS3Util;
    private final PostMapper postMapper;
    private final WishListMapper wishListMapper;
    private final RedisConfig redisConfig;

    // 회원가입 서비스
    public ResponseEntity<MessageResponseDto> createUser(SignUpRequestDto signUpRequestDto) {
        String nickname = signUpRequestDto.getNickname();
        String password = passwordEncoder.encode(signUpRequestDto.getPassword());
        String email = signUpRequestDto.getEmail();


//        checkDuplicatedEmail(email);
//        UserRoleEnum role = UserRoleEnum.USER;
//
//        private void checkDuplicatedEmail(String email) {
//            Optional<User> found = userRepository.findByEmail(email);
//            if (found.isPresent()) {
//                throw new InvalidConditionException(ErrorCodeEnum.DUPLICATE_USERNAME_EXIST);
//            }
//        }

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
//                .role(role)
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponseDto("회원가입 되었습니다.", HttpStatus.OK.toString()));
    }

    public UserResponsDto getUser(Long userId, Optional<UserDetailsImpl> userDetails) {
        // profile, user 합치고 연관관계 설정 후 정보 조회부분 리팩토링 필요
        // response 수정 필요

        User user = findUser(userId);

        UserInfoResponseDto userInfo = getUserInfo(user);

        boolean isfollow = userDetails.isPresent() && followReporitory.existsByFollowUserIdAndFollowedUserId(userId, userDetails.get().getUser().getId());

        List<FollowListResponseDto> followResponseDtoList = getFollowListResponseDtoList(userId).stream().limit(4).toList();
        List<PostListResponseDto> postResponseDtoList = getPostListResponseDtoList(userId).stream().limit(3).toList();

        if (userDetails.isPresent() && userDetails.get().getUser().getId().equals(userId)) {
            List<WishListResponseDto> wishlistResponseDtoList = getWishlistResponseDtoList(userId).stream().limit(3).toList();
            List<CommentListResponseDto> commentResponseDtoList = getCommentListResponseDtoList(userId).stream().limit(3).toList();

            UserResponsDto responsDto = new UserResponsDto(userInfo, postResponseDtoList, followResponseDtoList, commentResponseDtoList, wishlistResponseDtoList);

            return responsDto;
        }

        UserResponsDto responsDto = new UserResponsDto(userInfo, postResponseDtoList, followResponseDtoList, isfollow);

        return responsDto;
    }

    @Transactional
    public ResponseResource<?> updateUser(Long id, MultipartFile multipartFile,
                                          UserRequestDto requestDto,
                                          Long userId,
                                          HttpServletResponse response,
                                          HttpServletRequest request) {
        if (!userId.equals(id)) {
            throw new IllegalArgumentException("로그인한 유저가 아닙니다.");
        }
        User findUser = findUser(userId);

        String getUserImage = findUser.getUserImage();
        String getIntroduce = findUser.getIntroduce();
        String getNickname = findUser.getNickname();

        String originNickname = findUser.getNickname();


        if (requestDto == null) {
            requestDto = new UserRequestDto();
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
        log.info("수정 전 닉네임: {}, 수정 후 닉네임: {}", originNickname, getNickname);

        if (!originNickname.equals(getNickname)) {
            // redis에서 refreshToken 삭제
            redisUtil.removeRefreshToken(jwtUtil.getAccessTokenFromRequest(request));

            String accessToken = jwtUtil.createAccessToken(findUser.getEmail());
            String refreshToken = jwtUtil.createRefreshToken(findUser.getEmail());
            jwtUtil.addTokenToHeader(accessToken, refreshToken, response);
            // redis에 새로 발급받은 refreshToken 저장
            redisUtil.storeRefreshToken(accessToken, refreshToken);
        }
        return ResponseResource.data(getUserImage, HttpStatus.OK, "프로필 수정 성공");
    }

    @Transactional
    public ResponseResource<?> updatePassword(Long userId, PasswordRequestDto requestDto, User user) {
        String changePassword = requestDto.getChangePassword();

        User findUser = findUser(userId);
        if (!userId.equals(user.getId())) {
            throw new IllegalArgumentException("로그인한 유저가 아닙니다.");
        }

        if (!passwordEncoder.matches(requestDto.getOriginPassword(), user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }

        User changePassswordUser = User.builder()
                .password(passwordEncoder.encode(changePassword))
                .build();

        findUser.passwordUpdate(changePassswordUser);

        return ResponseResource.message("비밀번호 수정 성공", HttpStatus.OK);

    }

    public ResponseResource<?> checkNickname(NicknameRequestDto requestDto) {
        boolean checkNicknameDuplicate = userRepository.existsByNickname(requestDto.getNickname());
        // status code 수정 필요
        if (checkNicknameDuplicate) {
            return ResponseResource.message("닉네임 중복입니다.", HttpStatus.BAD_REQUEST);
        }

        return ResponseResource.message("사용 가능한 닉네임입니다.", HttpStatus.OK);
    }

    public GetFollowResponseDto getUserFollow(Long userId, Pageable pageable) {
        User user = findUser(userId);
        String nickname = user.getNickname();

        Page<FollowListResponseDto> followResponseDtoList = getFollowListResponseDtoList(userId, pageable);

        GetFollowResponseDto followResponseDto = new GetFollowResponseDto(nickname, followResponseDtoList);

        return followResponseDto;
    }

    public GetPostResponseDto getUserPosts(Long userId, Pageable pageable) {
        User user = findUser(userId);
        String nickname = user.getNickname();

        Page<PostListResponseDto> postResponseDtoList = getPostListResponseDtoList(userId, pageable);

        GetPostResponseDto postResponseDto = new GetPostResponseDto(nickname, postResponseDtoList);

        return postResponseDto;
    }

    public Page<CommentListResponseDto> getUserComments(Long userId, Optional<UserDetailsImpl> userDetails, Pageable pageable) {
        if (userDetails.isPresent() && userDetails.get().getUser().getId().equals(userId)) {
            Page<CommentListResponseDto> commentResponseDtoList = getCommentListResponseDtoList(userId, pageable);

            return commentResponseDtoList;
        }
        return null;
    }

    public Page<WishListResponseDto> getUserWishlist(Long userId, Optional<UserDetailsImpl> userDetails, Pageable pageable) {
        if (userDetails.isPresent() && userDetails.get().getUser().getId().equals(userId)) {
            Page<WishListResponseDto> wishlistResponseDtoList = getWishlistResponseDtoList(userId, pageable);

            return wishlistResponseDtoList;
        }
        return null;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 입니다"));
    }

    private List<FollowListResponseDto> getFollowListResponseDtoList(Long userId) {
        List<Follow> followList = followReporitory.findAllByFollowedUserId(userId);
        List<FollowListResponseDto> followResponseDtoList = followList.stream()
                .map(FollowListResponseDto::new)
                .toList();
        return followResponseDtoList;
    }

    private Page<FollowListResponseDto> getFollowListResponseDtoList(Long userId, Pageable pageable) {
        Page<Follow> followList = followReporitory.findAllByFollowedUserId(userId, pageable);
        Page<FollowListResponseDto> followResponseDtoList = followList.map(FollowListResponseDto::new);
        return followResponseDtoList;
    }

    // 내가 작성한 리스트 조회 -> deleted false ✅
    private List<PostListResponseDto> getPostListResponseDtoList(Long userId) {
        List<Post> postList = postRepository.findAllByUserIdAndDeletedFalse(userId);
        List<PostListResponseDto> postResponseDtoList = postList.stream()
                .map(postMapper::mapToPostListResponseDto)
                .collect(Collectors.toList());

        return postResponseDtoList;
    }

    private Page<PostListResponseDto> getPostListResponseDtoList(Long userId, Pageable pageable) {
//        Pageable pageable = PageRequest.of(page, size);

        Page<Post> postList = postRepository.findAllByUserIdAndDeletedFalse(userId, pageable);

        Page<PostListResponseDto> postResponseDtoList = postList.map(postMapper::mapToPostListResponseDto);

        return postResponseDtoList;
    }



    // 좋아요 한 리스트 조회 -> deleted false ✅
    private List<WishListResponseDto> getWishlistResponseDtoList(Long userId) {
        List<Wishlist> wishList = wishlistRepository.findAllByUserIdAndPostDeletedFalse(userId);
        List<WishListResponseDto> wishListReponseList = wishList.stream()
                .map(wishlist -> wishListMapper.mapToWishListResponseDto(wishlist.getPost()))
                .collect(Collectors.toList());

        return wishListReponseList;
    }

    private Page<WishListResponseDto> getWishlistResponseDtoList(Long userId, Pageable pageable) {
        Page<Wishlist> wishList = wishlistRepository.findAllByUserIdAndPostDeletedFalse(userId, pageable);
        Page<WishListResponseDto> wishListReponseList = wishList.map(wishlist -> wishListMapper.mapToWishListResponseDto(wishlist.getPost()));

        return wishListReponseList;
    }

    private List<CommentListResponseDto> getCommentListResponseDtoList(Long userId) {
        List<Comment> commentList = commentRepository.findAllByUserIdAndDeletedFalse(userId);
        List<CommentListResponseDto> commentResponseDtoList = commentList.stream()
                .map(CommentListResponseDto::new)
                .toList();
        return commentResponseDtoList;
    }

    private Page<CommentListResponseDto> getCommentListResponseDtoList(Long userId, Pageable pageable) {
        Page<Comment> commentList = commentRepository.findAllByUserIdAndDeletedFalse(userId, pageable);
        Page<CommentListResponseDto> commentResponseDtoList = commentList.map(CommentListResponseDto::new);
        return commentResponseDtoList;
    }

    private UserInfoResponseDto getUserInfo(User user) {
        return UserInfoResponseDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .userImage(user.getUserImage())
                .introduce(user.getIntroduce())
                .build();
    }


      //로그아웃
    @Transactional
    public ResponseResource<?> logout(String accessToken){
        // 로그아웃 하고 싶은 토큰이 유효한 지 먼저 검증하기
        String refreshToken = redisUtil.getRefreshToken(accessToken);
        log.info("현재 accessToken 을 키 값으로 가지고 있는 refreshToken : {}" , refreshToken);
        if (!jwtUtil.validateAccessToken(jwtUtil.substringToken(accessToken))){
            throw new IllegalArgumentException("로그아웃 : 유효하지 않은 토큰입니다.");
        }

        // 해당 Access Token 유효시간을 가지고 와서 BlackList 에 저장하기
        Claims claims = jwtUtil.getUserInfoFromToken(jwtUtil.substringToken(accessToken));
        long expiration = claims.getExpiration().getTime(); //엑세스 토큰 만료시간  가져오기
        String userInfo = claims.getSubject();
        long expirationInMinutes = TimeUnit.MINUTES.convert(expiration, TimeUnit.MILLISECONDS);

        log.info("expiration in Minutes : {}", expirationInMinutes);

        // Redis에서 해당 refreshToken 삭제
        if (redisUtil.getRefreshToken(accessToken).equals(refreshToken)){
            redisUtil.removeRefreshToken(accessToken);
        }
        redisConfig.redisTemplate().opsForValue().set("BLACKLIST_KEY_" + userInfo, accessToken , expirationInMinutes, TimeUnit.MINUTES);

        log.info("Input refreshToken : {}", refreshToken);
        log.info("redis Save refreshToken : {} ",redisUtil.getRefreshToken(accessToken));

        return ResponseResource.message("로그아웃 완료했습니다", HttpStatus.OK);
    }
}