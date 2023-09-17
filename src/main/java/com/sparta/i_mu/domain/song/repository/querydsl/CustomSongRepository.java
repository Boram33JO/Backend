package com.sparta.i_mu.domain.song.repository.querydsl;

import com.sparta.i_mu.domain.song.entity.Song;

import java.util.List;

public interface CustomSongRepository {

    List<Song> findByCategoryIdOrderByPostCountDesc(Long categoryId);

}
