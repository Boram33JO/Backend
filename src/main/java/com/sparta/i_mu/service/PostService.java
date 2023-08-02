package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.requestDto.PostSaveRequestDto;
import com.sparta.i_mu.dto.requestDto.PostSearchRequestDto;
import com.sparta.i_mu.entity.Location;
import com.sparta.i_mu.entity.Post;
import com.sparta.i_mu.entity.Song;
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
    public ResponseEntity<?> createPost(PostSaveRequestDto postRequestDto) {
        // post 생성
        Post post = Post.builder()
                .content(postRequestDto.getContent())
                .category(postRequestDto.getCategory())
                .build();

        postRepository.save(post);

        // 위치 post에 저장
        Location location = postRequestDto.getLocation();
        locationRepository.save(location);
        post.addLocation(location);

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

}
