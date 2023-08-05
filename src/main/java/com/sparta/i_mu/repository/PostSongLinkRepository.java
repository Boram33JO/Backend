package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.PostSongLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostSongLinkRepository extends JpaRepository <PostSongLink, Long> {
    List<PostSongLink> findAllByPostId(Long id);
}
