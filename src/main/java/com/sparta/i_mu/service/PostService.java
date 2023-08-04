package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.requestDto.PostSaveRequestDto;
import com.sparta.i_mu.dto.requestDto.PostSearchRequestDto;
import com.sparta.i_mu.dto.responseDto.PostByCategoryResponseDto;
import com.sparta.i_mu.dto.responseDto.PostResponseDto;
import com.sparta.i_mu.entity.*;
import com.sparta.i_mu.mapper.LocationMapper;
import com.sparta.i_mu.mapper.SongMapper;
import com.sparta.i_mu.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

import static com.sparta.i_mu.mapper.PostMapper.POST_INSTANCE;

// 전체 게시글 카테고리별 전체 조회 -> O
// 지도페이지에서 검색시 주변 게시글 조회
// 상세 게시글 조회 -> O
// 게시글 작성 -> O
// 게시글 수정 -> O
// 게시글 삭제 -> O

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final SongRepository songRepository;
    private final PostSongLinkRepository postSongLinkRepository;
    private final WishlistRepository wishlistRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private static final Double DISTANCE_IN_METERS = 500.0;

    //게시글 생성
    public ResponseEntity<?> createPost(PostSaveRequestDto postSaveRequestDto, User user) {
        if(user == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 후 이용이 가능합니다.");
        }

        Location location = LocationMapper.LOCATION_INSTANCE.dtoToEntity(postSaveRequestDto);
        Category category = categoryRepository.findByName(postSaveRequestDto.getCategory()).orElseThrow(
                ()-> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));

        locationRepository.save(location);
        // post 생성
        Post post = Post.builder()
                .content(postSaveRequestDto.getContent())
                .category(category)
                .user(user)
                .location(location)
                .build();

        postRepository.save(post);

        // 노래 list Song에 저장 후 각 PostSongLink 생성
        postSaveRequestDto.getSongs().stream()
                .map(songSaveRequestDto -> {
                    Song song = SongMapper.SONG_INSTANCE.responseDtoToEntity(songSaveRequestDto);
                    return songRepository.save(song);
                }).forEach(post::addPostSongLink);

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

        // post의 song의 연결 주체인 postSongLink 삭제
        post.removeSongs();

        postRequestDto.getSongs().stream()
                .map(songSaveRequestDto -> {
                    Song song = SongMapper.SONG_INSTANCE.responseDtoToEntity(songSaveRequestDto);
                    return songRepository.save(song);
                }).forEach(post::addPostSongLink);

        post.update(postRequestDto);
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
    public ResponseEntity<?> deletePost(Long postId, User user) throws AccessDeniedException {

        Post post = findPost(postId);
        checkAuthority(post,user);
        postRepository.delete(post);
        return ResponseEntity.status(HttpStatus.OK).body("해당 게시글 삭제를 완료하였습니다.");
    }


    // 메인페이지 관련
    @Transactional(readOnly = true)
    // 카테고리 별 전체 게시글 조회 3개 최신순
    public List<PostByCategoryResponseDto> getAllPost() {

        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(category ->{
                    List<Post> posts = postRepository.findAllByCategoryOrderByCreatedAtDesc(category);
                    List<PostResponseDto> postResponseDtoList = posts.stream()
                            .map(this::mapToPostResponseDto)
                            .limit(3)
                            .collect(Collectors.toList());

                    return PostByCategoryResponseDto.builder()
                            .category(category.getName()) // check 현재는 객체로
                            .postByCategoryResponseDtoList(postResponseDtoList)
                            .build();

                }).collect(Collectors.toList());

    }

    // 좋아요 순 인기 게시글 내림차순 조회
    public List<PostResponseDto> getPostByWishlist() {
        return postRepository.findAllByOrderByWishlistCountDesc().stream()
                .map(this::mapToPostResponseDto)
                .limit(5)
                .collect(Collectors.toList());

    }

    // 서브게시물 페이지

    // 서브 게시글 조회 - 내 주변
    @Transactional(readOnly = true)
    public List<PostResponseDto> getAllAreaPost(PostSearchRequestDto postSearchRequestDto, User user) {

        Double longitude = postSearchRequestDto.getLongitude();
        Double latitude = postSearchRequestDto.getLatitude();

        List<Post> posts = postRepository.findAllByLocationNear(latitude, longitude, DISTANCE_IN_METERS);
        return posts.stream()
                .map(this::mapToPostResponseDto)
                .collect(Collectors.toList());
    }

    //서브 게시글 조회 - 카테고리 별 전체 조회 기본(최신순)
    public List<PostResponseDto> getPostByCategory(String category, User user) {

        List<Post> posts = postRepository.findAllPostByCategoryNameOrderByCreatedAtDesc(category);
        return posts.stream()
                .map(this::mapToPostResponseDto)
                .collect(Collectors.toList());

    }

    //상세페이지 게시글 조회
    public PostResponseDto getDetailPost(Long postId, User user) {
        Post post = findPost(postId);
        return mapToPostResponseDto(post);
    }

    //지도 페이지
    public List<PostByCategoryResponseDto> getMapPostByCategory(PostSearchRequestDto postSearchRequestDto, User user) {

        Double longitude = postSearchRequestDto.getLongitude();
        Double latitude = postSearchRequestDto.getLatitude();

        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(category ->{
                    String name = category.getName();
                    List<Post> posts = postRepository.findAllByCategoryAndLocationNear(name,latitude, longitude, DISTANCE_IN_METERS);
                    List<PostResponseDto> postResponseDtoList = posts.stream()
                            .map(this::mapToPostResponseDto)
                            .collect(Collectors.toList());

                    return PostByCategoryResponseDto.builder()
                            .category(category.getName()) // check 현재는 객체로
                            .postByCategoryResponseDtoList(postResponseDtoList)
                            .build();

                }).collect(Collectors.toList());

    }


    // stream.map 안에서 wishlistCount값을 추가시켜 Dto로 변경하는 메서드
    private PostResponseDto mapToPostResponseDto(Post post){
        Long wishlistCount = wishlistRepository.countByPostId(post.getId());
        return POST_INSTANCE.entityToResponseDto(post, wishlistCount);
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
            if (post.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("작성자만 수정, 삭제가 가능합니다.");
            }
        }
//    }


}
