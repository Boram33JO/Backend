package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.requestDto.PostSaveRequestDto;
import com.sparta.i_mu.dto.requestDto.PostSearchRequestDto;
import com.sparta.i_mu.dto.responseDto.PostByCategoryResponseDto;
import com.sparta.i_mu.dto.responseDto.PostResponseDto;
import com.sparta.i_mu.entity.*;
import com.sparta.i_mu.mapper.LocationMapper;
import com.sparta.i_mu.mapper.PostMapper;
import com.sparta.i_mu.repository.*;
import com.sparta.i_mu.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
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
    private static final Double DISTANCE_IN_METERS = 500.0;

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
                .orElseThrow(()-> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다."));

        // post의 song의 연결 주체인 postSongLink 삭제
        post.removeSongs();
        //TODO 현재 게시물에서 노래 목록을 가져와 새로 업데이트 될 목록과 비교해서 각각 추가 삭제 하기
        postRequestDto.getSongs().stream()
                .map(songSaveRequestDto -> songRepository.findBySongNum(songSaveRequestDto.getSongNum())
                            .orElseGet(()->{
                                Song newSong = SONG_INSTANCE.requestDtoToEntity(songSaveRequestDto);
                                songRepository.save(newSong);
                                return newSong;
                            })
                ).map(post::addPostSongLink)
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


    // 좋아요 순 인기 게시글 내림차순 조회
    public List<PostResponseDto> getPostByWishlist() {
        return postRepository.findAllByOrderByWishlistCountDesc().stream()
                .map(postMapper::mapToPostResponseDto)
                .limit(5)
                .collect(Collectors.toList());

    }

    // 서브게시물 페이지

    // 서브 게시글 조회 - 내 주변

    public List<PostResponseDto> getAllAreaPost(PostSearchRequestDto postSearchRequestDto) {

        Double longitude = postSearchRequestDto.getLongitude();
        Double latitude = postSearchRequestDto.getLatitude();

        List<Post> posts = postRepository.findAllByLocationNear(latitude, longitude, DISTANCE_IN_METERS);
        return posts.stream()
                .map(postMapper::mapToPostResponseDto)
                .collect(Collectors.toList());
    }

    //서브 게시글 조회 - 카테고리 별 전체 조회 기본(최신순)
    public List<PostResponseDto> getPostByCategory(String category) {

        List<Post> posts = postRepository.findAllPostByCategoryNameOrderByCreatedAtDesc(category);
        return posts.stream()
                .map(postMapper::mapToPostResponseDto)
                .collect(Collectors.toList());

    }

    //상세페이지 게시글 조회
    public PostResponseDto getDetailPost(Long postId, Optional<UserDetailsImpl> userDetails) {
        Post post = findPost(postId);

        return postMapper.mapToPostResponseDto(post, userDetails);
    }

    //지도 페이지
    public List<PostByCategoryResponseDto> getMapPostByCategory(PostSearchRequestDto postSearchRequestDto) {

        Double longitude = postSearchRequestDto.getLongitude();
        Double latitude = postSearchRequestDto.getLatitude();

        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(category ->{
                    String name = category.getName();
                    List<Post> posts = postRepository.findAllByCategoryAndLocationNear(name,latitude, longitude, DISTANCE_IN_METERS);
                    List<PostResponseDto> postResponseDtoList = posts.stream()
                            .map(postMapper::mapToPostResponseDto)
                            .collect(Collectors.toList());
                    return PostByCategoryResponseDto.builder()
                            .category(category.getName()) // check 현재는 객체로
                            .postByCategoryResponseDtoList(postResponseDtoList)
                            .build();

                }).collect(Collectors.toList());

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



}
