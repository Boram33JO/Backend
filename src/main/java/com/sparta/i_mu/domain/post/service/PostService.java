package com.sparta.i_mu.domain.post.service;

import com.amazonaws.services.kms.model.NotFoundException;
import com.sparta.i_mu.domain.category.entity.Category;
import com.sparta.i_mu.domain.category.repository.CategoryRepository;
import com.sparta.i_mu.domain.location.entity.Location;
import com.sparta.i_mu.domain.location.repository.LocationRepository;
import com.sparta.i_mu.domain.post.entity.Post;
import com.sparta.i_mu.domain.post.repository.PostRepository;
import com.sparta.i_mu.domain.postsonglink.entity.PostSongLink;
import com.sparta.i_mu.domain.postsonglink.repository.PostSongLinkRepository;
import com.sparta.i_mu.domain.song.entity.Song;
import com.sparta.i_mu.domain.song.repository.SongRepository;
import com.sparta.i_mu.domain.user.entity.User;
import com.sparta.i_mu.domain.post.dto.MapPostSearchRequestDto;
import com.sparta.i_mu.domain.post.dto.PostSaveRequestDto;
import com.sparta.i_mu.domain.post.dto.PostByCategoryResponseDto;
import com.sparta.i_mu.domain.post.dto.PostResponseDto;
import com.sparta.i_mu.domain.post.dto.TopPostResponseDto;
import com.sparta.i_mu.global.errorCode.ErrorCode;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.util.RedisUtil;
import com.sparta.i_mu.domain.post.mapper.PostMapper;
import com.sparta.i_mu.global.security.UserDetailsImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static com.sparta.i_mu.domain.song.mapper.SongMapper.SONG_INSTANCE;


// 전체 게시글 카테고리별 전체 조회 -> O
// 지도페이지에서 검색시 주변 게시글 조회
// 상세 게시글 조회 -> O
// 게시글 작성 -> O
// 게시글 수정 -> O
// 게시글 삭제 -> O

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final SongRepository songRepository;
    private final PostSongLinkRepository postSongLinkRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final PostMapper postMapper;
    private final RedisUtil redisUtil;
    private static final Double DISTANCE_IN_METERS = 10000.0;

    //게시글 생성
    @Transactional
    public ResponseResource<?> createPost(PostSaveRequestDto postSaveRequestDto, User user) {
        if (user == null) {
            return ResponseResource.error2(ErrorCode.USER_UNAUTHORIZED);
        }

        Location location = Location.builder()
                .longitude(postSaveRequestDto.getLongitude())
                .latitude(postSaveRequestDto.getLatitude())
                .address(postSaveRequestDto.getAddress())
                .placeName(postSaveRequestDto.getPlaceName())
                .build();

        Category category = categoryRepository.findById(postSaveRequestDto.getCategory()).orElseThrow(
                () -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));

        locationRepository.save(location);
        // post 생성
        Post post = Post.builder()
                .postTitle(postSaveRequestDto.getPostTitle())
                .content(postSaveRequestDto.getContent())
                .category(category)
                .user(user)
                .location(location)
                .deleted(false)
                .build();

        postRepository.save(post);

        // 노래 list Song에 저장 후 각 PostSongLink 생성
        postSaveRequestDto.getSongs().stream()
                .map(songSaveRequestDto -> songRepository.findBySongNum(songSaveRequestDto.getSongNum())
                        .orElseGet(() -> {
                            Song newSong = SONG_INSTANCE.requestDtoToEntity(songSaveRequestDto);
                            songRepository.save(newSong);
                            return newSong;
                        })
                ).map(post::addPostSongLink)
                .forEach(postSongLinkRepository::save);

        //postId 반환 값 넣어주기
        Map<String, Long> response = new HashMap<>();
        response.put("postId", post.getId());
        return ResponseResource.data(response, HttpStatus.CREATED, "게시글이 생성되었습니다.");
    }

    /**
     * 게시글 수정
     *
     * @param postId
     * @param postRequestDto
     * @return 수정된 게시글
     */
    @Transactional
    public ResponseResource<?> updatePost(Long postId, PostSaveRequestDto postRequestDto, User user) throws AccessDeniedException {
        // post 존재 여부 확인
        Post post = findPost(postId);
        // 사용자 확인
        checkAuthority(post, user);

        Category newCategory = categoryRepository.findById(postRequestDto.getCategory())
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));

        // 1. 현재 post와 연결되어있는 song의 id조회
        Set<String> songsNum = fetchExistingSongsNum(post);
        // 2. 업데이트 될 song id 조회 후 없는 것은 추가 후 집합으로 가져오기
        Set<String> newSongsNum = fetchNewSongsNum(postRequestDto);
        // 3. 기존 노래 중 새로운 노래 목록에 없는 노래들 postSongLink삭제
        removeOldSongs(post, newSongsNum);
        // 4. 기존 노래에 없는 새로운 노래를 postSongLink에 추가
        addNewSongs(post, newSongsNum, songsNum);

        post.update(postRequestDto, newCategory);
        postRepository.save(post);
        return ResponseResource.message("게시물이 업데이트 되었습니다.", HttpStatus.OK);
    }


    // 1. 현재 post와 연결되어있는 song의 id조회
    private Set<String> fetchExistingSongsNum(Post post) {
        Set<String> songsNum = post.getPostSongLink().stream()
                .map(postSongLink -> postSongLink.getSong().getSongNum())
                .collect(Collectors.toSet());
        return songsNum;
    }

    // 2. 업데이트 될 song id 조회 후 없는 것은 추가 후 집합으로 가져오기
    private Set<String> fetchNewSongsNum(PostSaveRequestDto postRequestDto) {
        return postRequestDto.getSongs().stream()
                .map(songSaveRequestDto -> songRepository.findBySongNum(songSaveRequestDto.getSongNum())
                        .orElseGet(() -> {
                            Song newSong = SONG_INSTANCE.requestDtoToEntity(songSaveRequestDto);
                            songRepository.save(newSong);
                            return newSong;
                        })
                )
                .map(Song::getSongNum).collect(Collectors.toSet());
    }

    // 3. 기존 노래 중 새로운 노래 목록에 없는 노래들 postSongLink삭제
    private void removeOldSongs(Post post, Set<String> newSongsNum) {
        List<PostSongLink> linksToRemove = post.getPostSongLink().stream()
                .filter(postSongLink -> !newSongsNum.contains(postSongLink.getSong().getSongNum()))
                .toList();

        linksToRemove.forEach(link -> {
            post.getPostSongLink().remove(link);
            postSongLinkRepository.delete(link);
        });
    }

    // 4. 기존 노래에 없는 새로운 노래를 postSongLink에 추가
    private void addNewSongs(Post post, Set<String> newSongsNum, Set<String> songsNum) {

        newSongsNum.stream()
                .filter(songNum -> !songsNum.contains(songNum))
                .map(songNum -> songRepository.findBySongNum(songNum)
                        .orElseThrow(() -> new IllegalArgumentException("해당 곡은 존재하지 않습니다.")))
                .map(post::addPostSongLink)
                .forEach(postSongLinkRepository::save);
    }


    /**
     * 게시글 삭제
     *
     * @param postId
     * @param user
     * @return 게시글 삭제가 완료되었습니다 응답 메시지
     * @throws AccessDeniedException
     */
    @Transactional
    public ResponseResource<?> deletePost(Long postId, User user) throws AccessDeniedException {

        Post post = findPost(postId);
        checkAuthority(post, user);
        post.getComment().stream()
                .filter(comment -> !comment.getDeleted())
                .forEach(comment -> {
                    comment.setDeletedAt(LocalDateTime.now());
                    comment.setDeleted(true);
                });
        post.setDeletedAt(LocalDateTime.now());
        post.setDeleted(true);
        return ResponseResource.message("해당 게시글 삭제를 완료하였습니다.", HttpStatus.OK);

    }


    // 메인페이지 관련

    // 카테고리 별 전체 게시글 조회 3개 최신순 -> queryDsl 적용✅
    public List<PostByCategoryResponseDto> getAllPost() {

        List<Category> categories = categoryRepository.findAll();
        return categories.stream().sorted(Comparator.comparing(Category::getId))
                .map(category -> {
                    List<Post> posts = postRepository.findMainPostsByCategory(category);
                    List<PostResponseDto> postResponseDtoList = posts.stream()
                            .map(postMapper::mapToPostResponseDto)
                            .limit(3)
                            .collect(Collectors.toList());

                    return PostByCategoryResponseDto.builder()
                            .category(category.getId()) // check 현재는 객체로
                            .postByCategoryResponseDtoList(postResponseDtoList)
                            .build();

                }).collect(Collectors.toList());

    }

    // 좋아요 순 인기 게시글 내림차순 조회 top5 만 -> queryDsl 적용✅
    @Cacheable(value = "topPostCache", cacheManager = "redisCacheManager")
    public TopPostResponseDto getPostByTopList() {
        List<PostResponseDto> wishlistTopPosts = getTopPostsByWishlist();
        List<PostResponseDto> viewCountTopPosts = getTopPostsByViewCount();
        return new TopPostResponseDto(wishlistTopPosts, viewCountTopPosts);
    }

    public List<PostResponseDto> getTopPostsByWishlist() {
       return postRepository.findAllByOrderByWishlistCountDesc() .stream()
                .map(postMapper::mapToPostResponseDto)
//                .limit(5)
                .collect(Collectors.toList());
    }

    public List<PostResponseDto> getTopPostsByViewCount() {
        return postRepository.findAllByOrderByViewCountDesc() .stream()
                .map(postMapper::mapToPostResponseDto)
//                .limit(5)
                .collect(Collectors.toList());
    }
    // 서브게시물 페이지

    // 서브 게시글 조회 - 내 주변 -> queryDsl 적용✅
    public Page<PostResponseDto> getAllAreaPost(MapPostSearchRequestDto postSearchRequestDto, Pageable pageable) {


        Double longitude = postSearchRequestDto.getLongitude();
        Double latitude = postSearchRequestDto.getLatitude();

        Page<Post> posts = postRepository.findAllByLocationNearOrderByCreatedAtDesc(longitude,latitude, DISTANCE_IN_METERS, pageable);
        return posts.map(postMapper::mapToPostResponseDto);
    }

    //서브 게시글 조회 - 카테고리 별 전체 조회 기본(최신순) -> queryDsl 적용✅
    public Page<PostResponseDto> getPostByCategory(Long category, Pageable pageable) {
        Page <Post> posts = postRepository.findSubPostsByCategoryWithOrder(category, pageable);
        return posts.map(postMapper::mapToPostResponseDto);

    }

    @Transactional
    //상세페이지 게시글 조회
    public PostResponseDto getDetailPost(Long postId, Optional<UserDetailsImpl> userDetails, HttpServletRequest req, HttpServletResponse res) {
        Post post = postRepository.findByIdAndDeletedFalseForUpdate(postId).orElseThrow(() ->
                new NotFoundException(ErrorCode.POST_NOT_EXIST.getMessage()));
        String value = String.valueOf(postId);

//      로그인 유저는 USER ID, 비로그인 유저는 IP 주소
        String key = getUserIp(req);
        if (userDetails.isPresent()) {
            key = String.valueOf(userDetails.get().getUserId());
        }

        if (!redisUtil.getPostViewList(key).contains(value)) {
            redisUtil.setPostViewList(key, postId);
            post.viewCountUpdate();
        }

        return postMapper.mapToPostResponseDto(post, userDetails);
    }


    //지도 페이지 New Version
    public List<PostResponseDto> getMapPost(MapPostSearchRequestDto postSearchRequestDto, int size) {
        Double longitude = postSearchRequestDto.getLongitude();
        Double latitude = postSearchRequestDto.getLatitude();
        List<Post> posts = postRepository.findAllByLocationNear(longitude, latitude, DISTANCE_IN_METERS, size);
        return posts.stream()
                .map(postMapper::mapToPostResponseDto)
                .collect(Collectors.toList());
    }

    // 수정, 삭제 할 게시물이 존재하는지 확인하는 메서드
    public Post findPost(Long postId) {
        return postRepository.findByIdAndDeletedFalse(postId).orElseThrow(() ->
                new NotFoundException(ErrorCode.POST_NOT_EXIST.getMessage()));
    }

    // 수정, 삭제 할 게시물의 권한을 확인하는 메서드
    public void checkAuthority(Post post, User user) throws AccessDeniedException {
        // userId 확인
        if (!post.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException(ErrorCode.USER_NOT_MATCH.getMessage());
        }
    }


    // 접속자 IP 조회
    private String getUserIp(HttpServletRequest req) {
        String ip = req.getHeader("X-FORWARDED-FOR");
        if (ip == null) {
            ip = req.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = req.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null) {
            ip = req.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = req.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = req.getRemoteAddr();
        }
        return ip;
    }
}
