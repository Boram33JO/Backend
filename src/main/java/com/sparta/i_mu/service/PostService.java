package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.requestDto.PostSaveRequestDto;
import com.sparta.i_mu.dto.requestDto.MapPostSearchRequestDto;
import com.sparta.i_mu.dto.responseDto.PostByCategoryResponseDto;
import com.sparta.i_mu.dto.responseDto.PostResponseDto;
import com.sparta.i_mu.entity.*;
import com.sparta.i_mu.mapper.LocationMapper;
import com.sparta.i_mu.mapper.PostMapper;
import com.sparta.i_mu.repository.*;
import com.sparta.i_mu.security.UserDetailsImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sparta.i_mu.mapper.SongMapper.SONG_INSTANCE;


// 전체 게시글 카테고리별 전체 조회 -> O
// 지도페이지에서 검색시 주변 게시글 조회
// 상세 게시글 조회 -> O
// 게시글 작성 -> O
// 게시글 수정 -> O
// 게시글 삭제 -> O

@Service
@RequiredArgsConstructor

public class PostService {

    private final PostRepository postRepository;
    private final SongRepository songRepository;
    private final PostSongLinkRepository postSongLinkRepository;
    private final WishlistRepository wishlistRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final PostMapper postMapper;
    private static final Double DISTANCE_IN_METERS = 2000.0;

    //게시글 생성
    @Transactional
    public ResponseEntity<?> createPost(PostSaveRequestDto postSaveRequestDto, User user) {
        if(user == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 후 이용이 가능합니다.");
        }

        Location location = LocationMapper.LOCATION_INSTANCE.dtoToEntity(postSaveRequestDto);
        Category category = categoryRepository.findById(postSaveRequestDto.getCategory()).orElseThrow(
                ()-> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));

        locationRepository.save(location);
        // post 생성
        Post post = Post.builder()
                .postTitle(postSaveRequestDto.getPostTitle())
                .content(postSaveRequestDto.getContent())
                .category(category)
                .user(user)
                .location(location)
                .build();

        postRepository.save(post);

        // 노래 list Song에 저장 후 각 PostSongLink 생성
        postSaveRequestDto.getSongs().stream()
                .map(songSaveRequestDto -> songRepository.findBySongNum(songSaveRequestDto.getSongNum())
                .orElseGet(()->{
                    Song newSong = SONG_INSTANCE.requestDtoToEntity(songSaveRequestDto);
                    songRepository.save(newSong);
                    return newSong;})
                ).map(post::addPostSongLink)
                .forEach(postSongLinkRepository::save);

        return ResponseEntity.status(HttpStatus.CREATED).body("게시물 등록이 완료되었습니다.");
    }

    /**
     * 게시글 수정
     * @param postId
     * @param postRequestDto
     * @return 수정된 게시글
     */
    @Transactional
    public ResponseEntity<?> updatePost(Long postId, PostSaveRequestDto postRequestDto, User user) throws AccessDeniedException {
        // post 존재 여부 확인
        Post post = findPost(postId);
        // 사용자 확인
        checkAuthority(post, user);

        Category newCategory = categoryRepository.findById(postRequestDto.getCategory())
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));

        // 현재 post와 연결되어있는 song의 id조회
        Set<String> songsNum = post.getPostSongLink().stream()
                .map(postSongLink -> postSongLink.getSong().getSongNum())
                .collect(Collectors.toSet());

        // 업데이트 될 song id 조회 후 없는 것은 추가 후 집합으로 가져오기
        Set<String> newSongsNum = postRequestDto.getSongs().stream()
                .map(songSaveRequestDto -> songRepository.findBySongNum(songSaveRequestDto.getSongNum())
                        .orElseGet(() -> {
                            Song newSong = SONG_INSTANCE.requestDtoToEntity(songSaveRequestDto);
                            songRepository.save(newSong);
                            return newSong;
                        })
                )
                .map(Song::getSongNum).collect(Collectors.toSet());

        // 기존 노래 중 새로운 노래 목록에 없는 노래들 postSongLink삭제
        post.getPostSongLink().removeIf(postSongLink -> !newSongsNum.contains(postSongLink.getSong().getSongNum()));

        // 기존 노래에 없는 새로운 노래를 postSongLink에 추가
        newSongsNum.stream()
                .filter(songNum -> !songsNum.contains(songNum))
                .map(songNum -> songRepository.findBySongNum(songNum)
                        .orElseThrow(()-> new IllegalArgumentException("해당 곡은 존재하지 않습니다.")))
                .map(post::addPostSongLink)
                .forEach(postSongLinkRepository::save);

        post.update(postRequestDto, newCategory);
        postRepository.save(post);
        return ResponseEntity.status(HttpStatus.OK).body("게시물이 업데이트 되었습니다.");
    }

    /**
     * 게시글 삭제
     * @param postId
     * @param user
     * @return 게시글 삭제가 완료되었습니다 응답 메시지
     * @throws AccessDeniedException
     */
    @Transactional
    public ResponseEntity<String> deletePost(Long postId, User user) throws AccessDeniedException {

        Post post = findPost(postId);
        checkAuthority(post,user);
        postRepository.delete(post);
        return ResponseEntity.status(HttpStatus.OK).body("해당 게시글 삭제를 완료하였습니다.");
    }


    // 메인페이지 관련

    // 카테고리 별 전체 게시글 조회 3개 최신순
    public List<PostByCategoryResponseDto> getAllPost() {

        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(category ->{
                    List<Post> posts = postRepository.findAllByCategoryOrderByCreatedAtDesc(category);
                    List<PostResponseDto> postResponseDtoList = posts.stream()
                            .map(postMapper::mapToPostResponseDto)
                            .limit(3)
                            .collect(Collectors.toList());

                    return PostByCategoryResponseDto.builder()
                            .category(category.getName()) // check 현재는 객체로
                            .postByCategoryResponseDtoList(postResponseDtoList)
                            .build();

                }).collect(Collectors.toList());

    }

    // 메인 페이지 - 검색

    public Page<PostResponseDto> getSearch(String keyword, String type, Pageable pageable) {
        switch (type) {
            case "all" -> {
                Page<Post> postsAll = postRepository.findAll(keyword, pageable);
                return postsAll.map(postMapper::mapToPostResponseDto);
            }
            case "title" -> {
                postRepository.findAllByPostTitleContaining(keyword, pageable);
                Page<Post> postsTitle = postRepository.findAllByPostTitleContaining(keyword, pageable);
                return postsTitle.map(postMapper::mapToPostResponseDto);
            }
            case "nickname" -> {
                Page<Post> UserNickname = postRepository.findAllByUserNicknameContaining(keyword, pageable);
                return UserNickname.map(postMapper::mapToPostResponseDto);
            }
            case "songName" -> {
                Page<Post> songName = postRepository.findAllBySongTitleContaining(keyword, pageable);
                return songName.map(postMapper::mapToPostResponseDto);
            }
            default -> throw new IllegalArgumentException("해당 타입에서는 게시글을 찾을 수 없습니다. type: " + type);
        }
    }

    // 좋아요 순 인기 게시글 내림차순 조회 top5 만
    public List<PostResponseDto> getPostByWishlist() {
        return postRepository.findAllByOrderByWishlistCountDesc().stream()
                .map(postMapper::mapToPostResponseDto)
                .limit(5)
                .collect(Collectors.toList());

    }

    // 서브게시물 페이지

    // 서브 게시글 조회 - 내 주변
    public Page<PostResponseDto> getAllAreaPost(MapPostSearchRequestDto postSearchRequestDto, Pageable pageable) {


        Double longitude = postSearchRequestDto.getLongitude();
        Double latitude = postSearchRequestDto.getLatitude();

        Page<Post> posts = postRepository.findAllByLocationNearOrderByCreatedAtDesc(latitude, longitude, DISTANCE_IN_METERS, pageable);
        return posts.map(postMapper::mapToPostResponseDto);
    }

    //서브 게시글 조회 - 카테고리 별 전체 조회 기본(최신순)
    public Page<PostResponseDto> getPostByCategory(Long category, Pageable pageable) {
        Page <Post> posts = postRepository.findAllPostByCategoryIdOrderByCreatedAtDesc(category, pageable);
        return posts.map(postMapper::mapToPostResponseDto);

    }

    @Transactional
    //상세페이지 게시글 조회
    public PostResponseDto getDetailPost(Long postId, Optional<UserDetailsImpl> userDetails, HttpServletRequest req, HttpServletResponse res) {
        Post post = findPost(postId);

        // redis 추가 되면 전환
        // cookie 만료 시간이 추가 될 때 마다 갱신, 24:00:00 으로 수정
        postCountUpdate(post, req, res);

        return postMapper.mapToPostResponseDto(post, userDetails);
    }


    //지도 페이지

    public Page<PostResponseDto> getMapPostByCategory(MapPostSearchRequestDto postSearchRequestDto, Optional <Long> categoryId, Pageable pageable) {
        Double longitude = postSearchRequestDto.getLongitude();
        Double latitude = postSearchRequestDto.getLatitude();

        if (categoryId.isPresent()) {
            //해당 카테고리 조회
            Category category = categoryRepository.findById(categoryId.get()).orElseThrow(
                    () -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));

            Page<Post> posts = postRepository.findAllByCategoryAndLocationNear(category.getName(), latitude, longitude, DISTANCE_IN_METERS, pageable);
            return posts.map(postMapper::mapToPostResponseDto);
        }
        // 전체 카테고리 조회
        else {
            Page<Post> posts = postRepository.findAllByLocationNear(latitude, longitude, DISTANCE_IN_METERS,pageable);
            return posts.map(postMapper::mapToPostResponseDto);
        }
    }

    // 수정, 삭제 할 게시물이 존재하는지 확인하는 메서드
    public Post findPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(() ->
                new NullPointerException("존재하지 않는 게시글입니다."));
    }

    // 수정, 삭제 할 게시물의 권한을 확인하는 메서드

    public void checkAuthority(Post post, User user) throws AccessDeniedException {
        // admin 확인
//        if (!user.getRole().getAuthority().equals("ROLE_ADMIN")) {
        // userId 확인
        if (!post.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("작성자만 수정, 삭제가 가능합니다.");
        }
    }
//    }

    // 게시글 조회수 증가 메서드
    private void postCountUpdate(Post post, HttpServletRequest req, HttpServletResponse res) {
        Cookie oldCookie = null;

        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("postCount")) {
                    oldCookie = cookie;
                }
            }
        }

        if (oldCookie != null) {
            if (!oldCookie.getValue().contains("[" + post.getId() + "]")) {
                post.countUpdate();
                oldCookie.setValue(oldCookie.getValue() + "_[" + post.getId() + "]");
                oldCookie.setPath("/");
                oldCookie.setMaxAge(60 * 60 * 24);
                res.addCookie(oldCookie);
            }
        } else {
            post.countUpdate();
            Cookie newCookie = new Cookie("postCount","[" + post.getId() + "]");
            newCookie.setPath("/");
            newCookie.setMaxAge(60 * 60 * 24);
            res.addCookie(newCookie);
        }

    }



}
