package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowReporitory extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowUserIdAndFollowedUserId(Long followUserId, Long followedUserId);

    List<Follow> findAllByFollowedUserId(Long followedUserId);

    boolean existsByFollowUserIdAndFollowedUserId(Long followUserId, Long followedUserId);
}
