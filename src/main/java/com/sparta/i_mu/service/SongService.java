package com.sparta.i_mu.service;

import com.neovisionaries.i18n.CountryCode;
import com.sparta.i_mu.dto.responseDto.SongByCategoryResponseDto;
import com.sparta.i_mu.dto.responseDto.SongResponseDto;
import com.sparta.i_mu.entity.Song;
import com.sparta.i_mu.global.exception.NoContentException;
import com.sparta.i_mu.global.util.SpotifyUtil;
import com.sparta.i_mu.mapper.SongMapper;
import com.sparta.i_mu.repository.CategoryRepository;
import com.sparta.i_mu.repository.PostSongLinkRepository;
import com.sparta.i_mu.repository.SongRepository;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.data.search.simplified.SearchTracksRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
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
    private final CategoryRepository categoryRepository;
    private final SongMapper songMapper;

    /**
     * spotify 에서 노래 찾기
     * @param keyword
     * @return keyword 에 해당하는 노래 정보
     */
    public List<SongResponseDto> getSearch(String keyword) {
        try {

            log.info("Spotify API 액세스 토큰 요청 중...");
            String accessToken = spotifyUtil.getAccessToken();
            spotifyApi.setAccessToken(accessToken);
            log.info("액세스 토큰 발급 완료: {}", accessToken);

            //만약 띄워쓰기가 존재한다면 없애줘야함
            String key = keyword.replace(" ","");
            log.info("검색 키워드: 원본 [{}], 수정 [{}]", keyword, key);
            SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(key)
                    .market(CountryCode.KR)
                    .limit(10)
                    .offset(0)
                    .build();

            Paging<Track> SearchResult = searchTracksRequest.execute();
            Track[] tracks = SearchResult.getItems();

            if(tracks == null || tracks.length == 0){
                log.warn("검색 결과 없음: 키워드 [{}]", key);
                throw new NoContentException("찾으시는 노래가 존재하지 않습니다. 입력 값을 좀 더 자세히 입력해주세요!");
            }

            log.info("첫 번째 노래 제목: {}", tracks[0].getName());

            return Arrays.stream(tracks)
                    .map(track -> {
                        String songId = track.getId();
                        String title = track.getName();
                        AlbumSimplified album = track.getAlbum();
                        String albumName = album.getName();
                        ArtistSimplified[] artists = album.getArtists();
                        String artistName = artists[0].getName();
                        String audio = track.getPreviewUrl();
                        //앨범이미지
                        Image[] images = album.getImages();
                        String thumbnail = (images.length > 0) ? images[0].getUrl() : "NO_IMAGE";

                        ExternalUrl external_url = track.getExternalUrls();
                        String url = external_url.get("spotify"); // 외부 url 연결 키

                        return SongResponseDto.builder()
                                .songNum(songId)
                                .artistName(artistName)
                                .album(albumName)
                                .thumbnail(thumbnail)
                                .songTitle(title)
                                .audioUrl(audio)
                                .externalUrl(url)
                                .build();

                    })
                    .collect(Collectors.toList());

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new IllegalArgumentException("Error: " + e.getMessage());
        }
    }

    /**
     * 카테고리별 포스팅이 많이 된 노래 4개
     * @return
     */
    public List<SongByCategoryResponseDto> getMostPostSong(){

        List<Long> categoryIds = categoryRepository.findIds();

        return categoryIds.stream()
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
