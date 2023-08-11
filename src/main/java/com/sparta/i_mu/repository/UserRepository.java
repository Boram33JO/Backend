package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname);

    Optional<User> findByEmail(String Email);

    boolean existsByNickname(String Nickname);

    Optional<User> findByKakaoId(Long kakaoId);
}