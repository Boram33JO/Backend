package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
