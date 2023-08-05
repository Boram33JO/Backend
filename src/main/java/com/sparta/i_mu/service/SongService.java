package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.responseDto.SongResponseDto;
import com.sparta.i_mu.global.util.SpotifyUtil;
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
    public List<SongResponseDto> getSearch(String keyword) {
        try {
            // 토큰을 사용하기 전에 CreateToken을 이용해서 유효한 토큰을 가져옵니다.
            String accessToken = spotifyUtil.getAccessToken();
            // 가져온 액세스 토큰을 Spotify API에 설정합니다.
            spotifyApi.setAccessToken(accessToken);
            log.info("토크 발급완료");
            //만약 띄워쓰기가 존재한다면 없애줘야함
            String key = keyword.replace(" ","");
            SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(key)
                    .limit(10)
                    .build();
            Paging<Track> SearchResult = searchTracksRequest.execute();
            Track[] tracks = SearchResult.getItems();
            log.info("track결과 확인" + tracks[0].getName());

            return Arrays.stream(tracks)
                    .map(track -> {
                        String songId = track.getId();
                        String title = track.getName();
                        AlbumSimplified album = track.getAlbum();
                        String albumName = album.getName();
                        ArtistSimplified[] artists = album.getArtists();
                        String artistName = artists[0].getName();
                        String thumbnail = track.getAlbum().getUri();

                        ExternalUrl external_url = track.getExternalUrls();
                        String url = external_url.get("spotify");

                        return SongResponseDto.builder()
                                .songId(songId)
                                .artistName(artistName)
                                .album(albumName)
                                .thumbnail(thumbnail)
                                .title(title)
                                .external_url(url)
                                .build();
                    })
                    .collect(Collectors.toList());

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new IllegalArgumentException("Error: " + e.getMessage());
        }
    }
}
