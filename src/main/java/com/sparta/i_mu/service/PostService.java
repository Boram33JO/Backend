package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.requestDto.PostSaveRequestDto;
import com.sparta.i_mu.dto.requestDto.PostSearchRequestDto;
import com.sparta.i_mu.dto.responseDto.PostResponseDto;
import com.sparta.i_mu.entity.Location;
import com.sparta.i_mu.entity.Post;
import com.sparta.i_mu.entity.Song;
import com.sparta.i_mu.entity.User;
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

// 주변 게시글 카테고리별 전체 조회 -> O
// 상세 게시글 조회 -> O
// 게시글 작성 -> O
// 게시글 수정 -> O
// 게시글 삭제 -> O
// 좋아요 순 게시글 조회
// 좋아요 순 반대 게시글 조회
// 최근 순 게시글 조회
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

    /**
     * post 생성 메서드
     * @param postRequestDto
     * @return 완료 응답 메세지
     */
    public ResponseEntity<?> createPost(PostSaveRequestDto postRequestDto, User user) {
        // user가 null이 아닐 경우에만 게시글을 작성
        if(user == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 후 이용이 가능합니다.");
        }

        // 위치 post에 저장
        Location location = postRequestDto.getLocation();
        locationRepository.save(location);

        // post 생성
        Post post = Post.builder()
                .content(postRequestDto.getContent())
                .category(postRequestDto.getCategory())
                .user(user)
                .location(location)
                .build();

        postRepository.save(post);

        // 노래 list Song에 저장 후 각 PostSongLink 생성
        postRequestDto.getSongs().stream()
                .map(songSaveRequestDto -> {
                    Song song = Song.builder()
                            .artist(songSaveRequestDto.getArtist())
                            .title(songSaveRequestDto.getTitle())
                            .thumbnailImage(songSaveRequestDto.getThumbnail())
                            .build();
                    return songRepository.save(song);

                }).forEach(post::addPostSongLink);

        return ResponseEntity.status(HttpStatus.CREATED).body("게시물 등록이 완료되었습니다.");
    }

    //상세 게시글 조회
    public PostResponseDto getDetailPost(Long postId) {
        Post post = findPost(postId);
        return mapToPostResponseDto(post);
    }

    /**
     * 위치 정보를 동의 여부에 따른 카테고리 별 게시물 조회서비스
     * @param postSearchRequestDto
     * @return 근처에 해당하는 카테고리별 게시물들
     */
    @Transactional(readOnly = true)
    public List<?> getPostByCategory(PostSearchRequestDto postSearchRequestDto) {

        String category = postSearchRequestDto.getCategory();
        Double longitude = postSearchRequestDto.getLocation().getLongitude();
        Double latitude = postSearchRequestDto.getLocation().getLatitude();
//        if(!postSearchRequestDto.getLocationAgreed()) {
//            return Collections.singletonList(ResponseEntity.status(HttpStatus.FORBIDDEN).body("위치정보에 동의하신다면 더 많은 서비스를 이용하실 수 있습니다."));
//        }
        List<Post> posts = postRepository.findAllByCategoryAndLocationNear(category, latitude, longitude, DISTANCE_IN_METERS);
        return posts.stream()
                .map(this::mapToPostResponseDto)
                .collect(Collectors.toList());
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
                    Song song = Song.builder()
                            .artist(songSaveRequestDto.getArtist())
                            .title(songSaveRequestDto.getTitle())
                            .thumbnailImage(songSaveRequestDto.getThumbnail())
                            .build();

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

    // stream.map 안에서 Dto로 변경하는 메서드
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
//        // admin 확인
//        if (!user.getRole().getAuthority().equals("ROLE_ADMIN")) {
//            // userId 확인
//            if (post.getUser().getId() != user.getId()) {
//                throw new AccessDeniedException("작성자만 수정, 삭제가 가능합니다.");
//            }
//        }

        if (post.getUser().getId().equals(user.getId())){
            throw new AccessDeniedException("작성자만 수정, 삭제가 가능합니다.");
        }
    }
}
