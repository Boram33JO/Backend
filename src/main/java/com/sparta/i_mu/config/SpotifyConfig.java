package com.sparta.i_mu.config;

import com.wrapper.spotify.SpotifyApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpotifyConfig {
    @Value("${secret.CLIENT_ID}")
    private String clientId;

    @Value("${secret.CLIENT_SECRET}")
    private String clientSecret;

    @Bean
    public SpotifyApi spotifyApi() {
        return new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
    }
}
