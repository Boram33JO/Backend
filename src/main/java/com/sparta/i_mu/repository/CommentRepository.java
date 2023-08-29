package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    Page<Comment> findAllByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    List<Comment> findAllByPostIdAndDeletedFalse(Long id);

    Page<Comment> findAllByPostIdAndDeletedFalse(Long id, Pageable pageable);
}
