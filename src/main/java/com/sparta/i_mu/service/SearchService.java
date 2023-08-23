package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.responseDto.*;
import com.sparta.i_mu.entity.Post;
import com.sparta.i_mu.entity.Song;
import com.sparta.i_mu.entity.User;
import com.sparta.i_mu.global.exception.NoContentException;
import com.sparta.i_mu.global.util.RedisUtil;
import com.sparta.i_mu.mapper.PostMapper;
import com.sparta.i_mu.mapper.SongMapper;
import com.sparta.i_mu.repository.PostRepository;
import com.sparta.i_mu.repository.SongRepository;
import com.sparta.i_mu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final PostRepository postRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

    private final LocationService locationService;
    private final PostService postService;
    private final SongService songService;

    private final RedisUtil redisUtil;
    private final PostMapper postMapper;
    private final SongMapper songMapper;


    // 메인 페이지 - 검색

    /**
     * type 별 검색
     *
     * @param keyword
     * @param type
     * @param pageable
     * @return
     */
    public Page<?> getSearch(String keyword, String type, Pageable pageable) {
        redisUtil.storeSearchKeyword(keyword);
        switch (type) {
            case "title" -> {
                Page<Post> posts = postRepository.findAllByPostTitleContainingAndDeletedFalse(keyword, pageable);
                if (posts.isEmpty()) {
                    throw new NoContentException("No posts found with keyword: " + keyword);
                }
                return posts.map(postMapper::mapToPostResponseDto);
            }
            case "nickname" -> {
                Page<User> users = userRepository.findAllByNicknameContaining(keyword, pageable);
                if (users.isEmpty()) {
                    throw new NoContentException("No users found with keyword: " + keyword);
                }
                return users.map(user -> UserInfoResponseDto.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .introduce(user.getIntroduce())
                        .build());
            }
            case "songName" -> {
                Page<Song> songs = songRepository.findAllBySongTitleContaining(keyword, pageable);
                if (songs.isEmpty()) {
                    throw new NoContentException("No songs found with keyword: " + keyword);
                }
                return songs.map(songMapper::entityToResponseDto);
            }
            case "location" -> {
                Page<Post> postsByLocation = postRepository.findAllByLocationAddressContainingAndDeletedFalse(keyword, pageable);
                if (postsByLocation.isEmpty()) {
                    throw new NoContentException("No location found with keyword: " + keyword);
                }
                return postsByLocation.map(postMapper::mapToPostResponseDto);
            }
            default -> throw new IllegalArgumentException("검색 타입이 잘못되었습니다. type: " + type);
        }
    }

    /**
     * 전체 검색
     *
     * @param keyword
     * @return
     */
    public SearchResponseDto getSearchAll(String keyword, Pageable pageable) {
        redisUtil.storeSearchKeyword(keyword);
        // 노래, 유저는 정렬하지 않음.
        Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted());
        //Post 결과
        List<Post> postResults = postRepository.findAllByPostTitleContainingAndDeletedFalse(keyword, pageable).getContent();
        List<PostResponseDto> postDtos = postResults.stream()
                .map(postMapper::mapToPostResponseDto)
                .toList();

        log.info("posts title 조회 : {}", postResults.stream().findFirst());
        log.info("posts 결과 개수 조회 : {} ", postDtos.size());
        //User 결과
        List<User> userResults = userRepository.findAllByNicknameContaining(keyword, unsortedPageable).getContent();
        List<UserInfoResponseDto> userDtos = userResults.stream()
                .map(user -> UserInfoResponseDto.builder()
                        .userId(user.getId())
                        .nickname(user.getNickname())
                        .introduce(user.getIntroduce())
                        .build())
                .sorted()
                .toList();
        log.info("user nickname 조회 : {}", userResults.stream().findFirst());
        log.info("user 결과 개수 조회 : {} ", userDtos.size());
        //Song 결과
        List<Song> songResults = songRepository.findAllBySongTitleContaining(keyword, unsortedPageable).getContent();
        List<SongResponseDto> songDtos = songResults.stream()
                .map(songMapper::entityToResponseDto)
                .toList();

        log.info("song title 조회 : {}", songResults.stream().findFirst());
        log.info("song 결과 개수 조회 : {} ", songDtos.size());

        List<Post> locationResults = postRepository.findAllByLocationAddressContainingAndDeletedFalse(keyword, pageable).getContent();
        List<PostResponseDto> loationDtos = locationResults.stream()
                .map(postMapper::mapToPostResponseDto)
                .toList();

        log.info("Location post 조회 : {}", locationResults.stream().findFirst());
        log.info("Location post 결과 개수 조회 : {} ", loationDtos.size());
        return SearchResponseDto.builder()
                .posts(postDtos)
                .users(userDtos)
                .songs(songDtos)
                .locations(loationDtos)
                .build();
    }



    public SearchMainResponseDto getSearchMain() {
        SearchMainResponseDto searchMainResponseDto = SearchMainResponseDto.builder()
                .topLocations(locationService.getTopLocations())
                .topSearchKeywords(redisUtil.getSearchKeyword())
                .topSongs(songService.getMostByCategoryPostSong())
                .topPosts(postService.getPostByWishlist()).build();

        return searchMainResponseDto;
    }
}

