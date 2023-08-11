package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname);

    Optional<User> findByEmail(String Email);

    boolean existsByNickname(String Nickname);

    @Query("SELECT f.followUser, COUNT(f) FROM Follow f GROUP BY 1 ORDER BY 2 DESC")
    List<User> findAllByOrderByFollowCountDesc();
}