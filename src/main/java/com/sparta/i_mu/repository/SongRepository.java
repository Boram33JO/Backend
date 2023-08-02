package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long> {

}
