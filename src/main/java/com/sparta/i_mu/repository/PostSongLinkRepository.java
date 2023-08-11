package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.PostSongLink;
import com.sparta.i_mu.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostSongLinkRepository extends JpaRepository <PostSongLink, Long> {
    List<PostSongLink> findAllByPostId(Long id);

    @Query("SELECT ps.song FROM PostSongLink ps JOIN ps.song GROUP BY ps.song ORDER BY COUNT(ps) DESC")
    List<Song> findTopSong();
}
