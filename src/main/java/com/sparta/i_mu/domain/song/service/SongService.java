package com.sparta.i_mu.domain.song.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.i_mu.domain.song.dto.SongByCategoryResponseDto;
import com.sparta.i_mu.domain.song.dto.SongResponseDto;
import com.sparta.i_mu.domain.song.entity.Song;
import com.sparta.i_mu.global.exception.NoContentException;
import com.sparta.i_mu.global.util.RedisUtil;
import com.sparta.i_mu.global.util.SpotifyUtil;
import com.sparta.i_mu.domain.song.mapper.SongMapper;
import com.sparta.i_mu.domain.category.repository.CategoryRepository;
import com.sparta.i_mu.domain.postsonglink.repository.PostSongLinkRepository;
import com.sparta.i_mu.domain.song.repository.SongRepository;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SongService {

    private final SpotifyApi spotifyApi;
    private final SpotifyUtil spotifyUtil;
    private final SongRepository songRepository;
    private final PostSongLinkRepository postSongLinkRepository;
    private final CategoryRepository categoryRepository;
    private final SongMapper songMapper;
    private final RedisUtil redisUtil;

    @Value("${default.image.2}")
    private String imageUrl;
    /**
     * spotify 에서 노래 찾기
     *
     * @param keyword
     * @return keyword 에 해당하는 노래 정보
     */
    public List<SongResponseDto> getSearch(String keyword) {

        log.info("keyword : {}", keyword);
        // 먼저 값이 있다면 redis 에서 가져오기
        List<SongResponseDto> result = getFromRedis(keyword);
        log.info("Redis에 저장되어있는 검색 값 : {}", result);
        if (result == null || result.isEmpty()) {
            log.info("저장된 검색 결과가 존재하지 않을 때");
            result = searchFromSpotify(keyword);
            if(result != null && !result.isEmpty()){
                saveToRedis(keyword, result);
            }
        }
        return result;
    }

    /**
     *  Redis에서 데이터 가져오는 로직
     * @param keyword
     * @return redis에 저장되어있는 노래 리스트
     */
    private List<SongResponseDto> getFromRedis(String keyword) {
        String cachedResult = redisUtil.getSearchedSong(keyword);
        if (cachedResult != null) {
            try {
                return new ObjectMapper().readValue(cachedResult, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize data from Redis for keyword: {}", keyword, e);
            }
        }
        return null;
    }

    /**
     * Redis에 값이 없을 경우 Spotify 에서 노래 검색하는 로직
     * @param keyword
     * @return 검색한 노래 리스트
     */
    private List<SongResponseDto> searchFromSpotify(String keyword) {
        List<SongResponseDto> songs;

        try {
            log.info("Spotify API 액세스 토큰 요청 중...");
            String accessToken = spotifyUtil.getAccessToken();
            spotifyApi.setAccessToken(accessToken);
            log.info("액세스 토큰 발급 완료: {}", accessToken);

            //만약 띄워쓰기가 존재한다면 없애줘야함
//            String key = keyword.replace(" ", "");
//            log.info("검색 키워드: 원본 [{}], 수정 [{}]", keyword, key);
            SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(keyword)
                    // .market(CountryCode.KR)
                    .limit(30)
                    .offset(0)
                    .build();

            Paging<Track> SearchResult = searchTracksRequest.execute();
            Track[] tracks = SearchResult.getItems();

            if (tracks == null || tracks.length == 0) {
                log.warn("검색 결과 없음: 키워드 [{}]", keyword);
                throw new NoContentException("찾으시는 노래가 존재하지 않습니다. 입력 값을 좀 더 자세히 입력해주세요!");
            }
            log.info("첫 번째 노래 제목: {}", tracks[0].getName());
            songs = Arrays.stream(tracks).map(this::convertTrackToSongResponseDto).collect(Collectors.toList());
        } catch (IOException | SpotifyWebApiException |
                 ParseException e) {
            throw new IllegalArgumentException("Error: " + e.getMessage());
        }
        return songs;
    }

    private SongResponseDto convertTrackToSongResponseDto(Track track) {
        AlbumSimplified album = track.getAlbum();
        String thumbnail = (album.getImages().length > 0) ? album.getImages()[0].getUrl() : imageUrl ;
        return SongResponseDto.builder()
                .songNum(track.getId())
                .artistName(album.getArtists()[0].getName())
                .album(album.getName())
                .thumbnail(thumbnail)
                .songTitle(track.getName())
                .audioUrl(track.getPreviewUrl())
                .externalUrl(track.getExternalUrls().get("spotify"))
                .build();
    }

    /**
     * 검색한 결과를 Redis에 저장하는 로직.
     * @param keyword
     * @param songs
     */
    private void saveToRedis(String keyword, List<SongResponseDto> songs) {
        try {
            redisUtil.storeSearchedSong(keyword, new ObjectMapper().writeValueAsString(songs));
            log.info("Redis에 검색한 노래 저장,: {} ", new ObjectMapper().writeValueAsString(songs));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize data for keyword: {}", keyword, e);
            throw new RuntimeException("검색한 데이터의 저장이 불가능합니다. ", e);
        }
    }

    /**
     * 전체에서 포스팅이 가장 많이 된 top10 노래 조회 - 노래 검색 페이지에서 사용
     *
     * @return 인기노래 10개
     */
    public List<SongResponseDto> getMostAllPostSong() {
        return postSongLinkRepository.findTopSong().stream()
                .map(songMapper::entityToResponseDto)
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 포스팅이 많이 된 노래 4개 - 메인 화면
     *
     * @return
     */
    public List<SongByCategoryResponseDto> getMostByCategoryPostSong() {

        List<Long> categoryIds = categoryRepository.findIds();

        return categoryIds.stream().sorted()
                .map(categoryId -> {
                    // 해당 카테고리에서 가장 많이 포스팅된 노래 4곡을 찾습니다.
                    List<Song> topPostedSongs = songRepository.findByCategoryIdOrderByPostCountDesc(categoryId);

                    List<SongResponseDto> songResponseDtos = topPostedSongs.stream()
                            .map(songMapper::entityToResponseDto)
                            .limit(4)
                            .collect(Collectors.toList());

                    return SongByCategoryResponseDto.builder()
                            .Category(categoryId)
                            .songResponseDtos(songResponseDtos)
                            .build();
                })

                .collect(Collectors.toList());
    }
}
