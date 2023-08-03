package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.requestDto.PostSaveRequestDto;
import com.sparta.i_mu.dto.requestDto.PostSearchRequestDto;
import com.sparta.i_mu.entity.Location;
import com.sparta.i_mu.entity.Post;
import com.sparta.i_mu.entity.Song;
import com.sparta.i_mu.entity.User;
import com.sparta.i_mu.repository.LocationRepository;
import com.sparta.i_mu.repository.PostRepository;
import com.sparta.i_mu.repository.PostSongLinkRepository;
import com.sparta.i_mu.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.sparta.i_mu.mapper.PostMapper.POST_INSTANCE;

// 현재 위치에 해당하는 노래 조회
// 주변 게시글 전체 조회
// 상세 게시글 조회
// 카테고리 별 게시글 조회
// 게시글 작성 -> O
// 게시글 수정
// 게시글 삭제
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
    private LocationRepository locationRepository;
    private static final Double DISTANCE_IN_METERS = 500.0;

    /**
     * post우선 생성후 lccation, Songs들을 주입
     * @param postRequestDto
     * @return 완료 메세지
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

    /**
     * 위치 정보를 동의 여부에 따른 카테고리 별 게시물 조회서비스
     * TODO 동의하지 않았을 경우 기본위치에 관한 카테고리를 보여주게 수정필요
     * @param postSearchRequestDto
     * @return 근처에 해당하는 카테고리별 게시물들
     */
    @Transactional(readOnly = true)
    public List<?> getPostByCategory(PostSearchRequestDto postSearchRequestDto) {

        String category = postSearchRequestDto.getCategory();
        Double longitude = postSearchRequestDto.getLocation().getLongitude();
        Double latitude = postSearchRequestDto.getLocation().getLatitude();

        if(!postSearchRequestDto.getLocationAgreed()) {
            return Collections.singletonList(ResponseEntity.status(HttpStatus.FORBIDDEN).body("위치정보에 동의하신다면 더 많은 서비스를 이용하실 수 있습니다."));
        }
        List<Post> posts = postRepository.findAllByCategoryAndLocationNear(category, latitude, longitude, DISTANCE_IN_METERS);
        return posts.stream()
                .map(POST_INSTANCE::entityToDto)
                .collect(Collectors.toList());
    }

    /**
     * 게시글 수정
     * @param postId
     * @param postRequestDto
     * @return 수정된 게시글
     */
    @Transactional
    public ResponseEntity<?> updatePost(Long postId, PostSaveRequestDto postRequestDto, User user) {

        Post post = postRepository.findById(postId)
                .orElseThrow(()-> new IllegalArgumentException("해당 게시물이 없습니다."));

        // 사용자 확인
        if(!post.getUser().getId().equals(user.getId())){
            throw new IllegalArgumentException("해당 게시물의 작성자만 수정이 가능합니다.");
        }

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

}
