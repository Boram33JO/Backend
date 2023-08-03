package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.requestDto.PostSaveRequestDto;
import com.sparta.i_mu.dto.requestDto.PostSearchRequestDto;
import com.sparta.i_mu.dto.responseDto.PostResponseDto;
import com.sparta.i_mu.entity.Location;
import com.sparta.i_mu.entity.Post;
import com.sparta.i_mu.entity.Song;
import com.sparta.i_mu.entity.User;
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

    private PostRepository postRepository;
    private SongRepository songRepository;
    private PostSongLinkRepository postSongLinkRepository;
    private WishlistRepository wishlistRepository;
    private LocationRepository locationRepository;
    private static final Double DISTANCE_IN_METERS = 500.0;

    //게시글 생성
    public ResponseEntity<?> createPost(PostSaveRequestDto postSaveRequestDto, User user) {
        if(user == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 후 이용이 가능합니다.");
        }

        // post안의 위치값을 Mapper로 entity로 변환 후 저장
        Location location = LocationMapper.LOCATION_INSTANCE.dtoToEntity(postSaveRequestDto);
        locationRepository.save(location);

        // post 생성
        Post post = Post.builder()
                .content(postSaveRequestDto.getContent())
                .category(postSaveRequestDto.getCategory())
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

    //상세 게시글 조회
    public PostResponseDto getDetailPost(Long postId) {
        Post post = findPost(postId);
        return mapToPostResponseDto(post);
    }

    @Transactional(readOnly = true)
    // 카테고리 별 전체 게시글 조회
    public List<PostResponseDto> getAllPost() {

        List<Post> posts = postRepository.findAllByCategoryWishlistCountDesc();
        return posts.stream()
                .map(this::mapToPostResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 위치 정보를 동의 여부에 따른 게시물 조회서비스
     * @param postSearchRequestDto
     * @return 근처에 해당하는 카테고리별 게시물들
     */
    @Transactional(readOnly = true)
    public List<?> getPostByCategory(PostSearchRequestDto postSearchRequestDto) {

        Double longitude = postSearchRequestDto.getLongitude();
        Double latitude = postSearchRequestDto.getLatitude();

        List<Post> posts = postRepository.findAllByLocationNear(latitude, longitude, DISTANCE_IN_METERS);
        return posts.stream()
                .map(this::mapToPostResponseDto)
                .collect(Collectors.toList());
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
