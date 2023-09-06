package com.sparta.i_mu.domain.user.service;

import com.sparta.i_mu.domain.comment.dto.CommentListResponseDto;
import com.sparta.i_mu.domain.comment.entity.Comment;
import com.sparta.i_mu.domain.comment.repository.CommentRepository;
import com.sparta.i_mu.domain.follow.dto.FollowListResponseDto;
import com.sparta.i_mu.domain.follow.entity.Follow;
import com.sparta.i_mu.domain.follow.repository.FollowReporitory;
import com.sparta.i_mu.domain.kakao.service.KakaoService;
import com.sparta.i_mu.domain.notification.repository.EmitterRepository;
import com.sparta.i_mu.domain.post.dto.PostListResponseDto;
import com.sparta.i_mu.domain.post.entity.Post;
import com.sparta.i_mu.domain.post.mapper.PostMapper;
import com.sparta.i_mu.domain.post.repository.PostRepository;
import com.sparta.i_mu.domain.user.dto.*;
import com.sparta.i_mu.domain.user.entity.User;
import com.sparta.i_mu.domain.user.repository.UserRepository;
import com.sparta.i_mu.domain.wishlist.dto.WishListResponseDto;
import com.sparta.i_mu.domain.wishlist.entity.Wishlist;
import com.sparta.i_mu.domain.wishlist.mapper.WishListMapper;
import com.sparta.i_mu.domain.wishlist.repository.WishlistRepository;
import com.sparta.i_mu.global.errorCode.ErrorCode;
import com.sparta.i_mu.global.exception.UserNotFoundException;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.security.UserDetailsImpl;
import com.sparta.i_mu.global.util.AwsS3Util;
import com.sparta.i_mu.global.util.JwtUtil;
import com.sparta.i_mu.global.util.RedisUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Date;
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

    private final EmitterRepository emitterRepository;

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final AwsS3Util awsS3Util;
    private final PostMapper postMapper;
    private final KakaoService kakaoService;
    private final WishListMapper wishListMapper;

    // 회원가입 서비스
    public ResponseEntity<MessageResponseDto> createUser(SignUpRequestDto signUpRequestDto) {
        String nickname = signUpRequestDto.getNickname();
        String password = passwordEncoder.encode(signUpRequestDto.getPassword());
        String email = signUpRequestDto.getEmail();
        String phoneNumber = signUpRequestDto.getPhoneNumber();

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

        Optional<User> checkPhonenumber = userRepository.findByPhoneNumber(phoneNumber);
        if (checkPhonenumber.isPresent()) {
            throw new IllegalArgumentException("중복된 전화번호 입니다.");
        }


        // 사용자 등록

        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password(password)
                .phoneNumber(phoneNumber)
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
            return ResponseResource.error("닉네임 중복입니다.", HttpStatus.BAD_REQUEST.value());
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
        List<Post> postList = postRepository.findAllByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId);
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
        List<Wishlist> wishList = wishlistRepository.findAllByUserIdAndPostDeletedFalseOrderByCreatedAtDesc(userId);
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
        List<Comment> commentList = commentRepository.findAllByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId);
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
    public ResponseResource<?> logout(String accessToken) {
        // 로그아웃 하고 싶은 토큰이 유효한 지 먼저 검증하기
        try {
            log.info("AccessToken : {}", accessToken);
            if (jwtUtil.isTokenExpired(jwtUtil.substringToken(accessToken))) {
                //토큰이 만료되었을경우
                redisUtil.removeRefreshToken(accessToken);
                return ResponseResource.message("로그아웃 완료했습니다", HttpStatus.OK);
            }
            // 해당 Access Token 유효시간을 가지고 와서 BlackList 에 저장하기
            Claims claims = jwtUtil.getUserInfoFromToken(jwtUtil.substringToken(accessToken));
            Long expiration = claims.getExpiration().getTime() - new Date().getTime(); // 엑세스 토큰 만료시간  가져오기
            log.info("expiration : {}", expiration);

            String userInfo = claims.getSubject();
            Long expirationInSeconds = TimeUnit.MILLISECONDS.toSeconds(expiration);

            log.info("expiration in MILLISECONDS : {}", expirationInSeconds);

            // 해당 accessToken - RefreshToken 삭제
            redisUtil.removeRefreshToken(accessToken);
            //새롭게 redis에 블랙리스트 저장 - 만료시간이 지났을때 블랙리스트도 삭제

            redisUtil.storeBlacklist(userInfo, accessToken, expirationInSeconds);

            // sse 로그아웃 임시 
//             Optional<User> findUser = userRepository.findByEmail(userInfo);
//             String id = String.valueOf(findUser.get().getId());
//             emitterRepository.deleteAllStartWithId(id);
//             emitterRepository.deleteAllEventCacheStartWithId(id);


            return ResponseResource.message("로그아웃 완료했습니다", HttpStatus.OK);

        } catch (Exception e) {
            log.error("로그아웃 중 오류 발생 : ", e);
            return ResponseResource.error2(ErrorCode.TOKEN_INVALID);
        }
    }

    /**
     * 회원 탈퇴 로직
     *
     * @param user
     * @return
     */
    @Transactional
    public ResponseResource<?> cancelUser(User user, HttpServletRequest req) {

        String AccessToken = jwtUtil.BEARER + jwtUtil.getAccessTokenFromRequest(req);
        try {
            // 먼저 회원이 데이터 베이스에 존재하는지
            User cancelUser = userRepository.findById(user.getId()).orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_AUTHENTICATED.getMessage()));
            Long cancelUserId = cancelUser.getId();
            log.info("삭제 할 User 의 아이디 : {}", cancelUser.getId());
            // 카카오 아이디가가 존재할 때
            if (cancelUser.getKakaoId() != null) {
                log.info("소셜 로그인 한 User 의 KakaoId {} :", cancelUser.getKakaoId());
                if (!unlinkKakao(cancelUser.getKakaoId())) {
                    return ResponseResource.error2(ErrorCode.KAKAO_UNLINK_FAILED);
                }
                if (!KakaoAllDelete(cancelUserId)) { // 변경된 부분
                    log.error("연결 해제 O / 데이터 삭제 X");
                    return ResponseResource.error("카카오 계정 연결 해제는 성공했으나, 회원 데이터 삭제에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
                }
                log.info("연결 해제 O / 데이터 삭제 O");
                return ResponseResource.message("카카오 계정 연결 해제 및 회원 탈퇴 처리가 완료되었습니다.", HttpStatus.OK);
            } else {
                log.info("일반 회원의 회원 탈퇴 로직");
                // 2. 카카오 아이디가 존재하지 않는 일반 회원일 때 - 게시글, 댓글 deleted처리
                deleteRegularUser(cancelUserId);
                markUserAsDeleted(cancelUser);
                redisUtil.removeRefreshToken(AccessToken);
            }

        } catch (DataAccessException e) {
            log.error("DB를 처리하는 과정에서 error 발생 : ", e);
            return ResponseResource.error2(ErrorCode.DATABASE_PROCESSING_ERROR);
        } catch (Exception e) {
            log.error("알 수 없는 오류가 발생 : ", e);
            return ResponseResource.error("알 수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return ResponseResource.message("회원 탈퇴 처리가 완료되었습니다.", HttpStatus.OK);
    }

    private boolean unlinkKakao(Long kakaoId) {
        return kakaoService.unlinkKakaoAccount(kakaoId);
    }

    private void deleteCommentsByUser(Long userId) {
        List<Comment> comments = commentRepository.findAllByUserIdAndDeletedFalse(userId);
        comments.forEach(comment -> {
            comment.setDeletedAt(LocalDateTime.now());
            comment.setDeleted(true);
            commentRepository.save(comment);
        });
    }

    private void deletePostsByUser(Long userId) {
        List<Post> posts = postRepository.findAllByUserIdAndDeletedFalse(userId);
        posts.forEach(post -> {
            post.setDeletedAt(LocalDateTime.now());
            post.setDeleted(true);
            postRepository.save(post);
        });
    }

    private void deleteWishlistsByUser(Long userId) {
        List<Wishlist> wishlists = wishlistRepository.findAllByUserId(userId);
        log.info("wishlists: {}", wishlists);
        wishlistRepository.deleteAll(wishlists);
    }

    private void deletedFollowByUser(Long userId) {
        List<Follow> follows = followReporitory.findAllByFollowUserId(userId);
        log.info("follows :{}", follows);
        followReporitory.deleteAll(follows);

        List<Follow> followed = followReporitory.findAllByFollowedUserId(userId);
        log.info("followed :{}", followed);
        followReporitory.deleteAll(followed);
    }

    private void markUserAsDeleted(User cancelUser) {
        cancelUser.setDeleted(true);
        cancelUser.setDeletedAt(LocalDateTime.now());
        cancelUser.setNickname();
        userRepository.save(cancelUser);
    }

    private boolean KakaoAllDelete(Long userId) {
        try {
            List<Post> posts = postRepository.findAllByUserIdAndDeletedFalse(userId);
            postRepository.deleteAll(posts);

            List<Comment> comments = commentRepository.findAllByUserIdAndDeletedFalse(userId);
            commentRepository.deleteAll(comments);

            deleteWishlistsByUser(userId);
            deletedFollowByUser(userId);

            User user = userRepository.findById(userId).orElseThrow(() ->
                    new UserNotFoundException("존재하지 않는 유저입니다."));

            userRepository.delete(user);
            return true; // 성공적으로 삭제될 경우
        } catch (Exception e) {
            log.error("KakaoAllDelete 중 오류가 발생", e);
            return false; // 예외 발생시 false 반환
        }
    }

    private void deleteRegularUser(Long userId) {
        deleteCommentsByUser(userId);
        deletePostsByUser(userId);
        deleteWishlistsByUser(userId);
        deletedFollowByUser(userId);
    }
}
