package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname);

    Optional<User> findByEmail(String Email);

    boolean existsByNickname(String Nickname);

    Optional<User> findByKakaoId(Long kakaoId);

    @Query("SELECT f.followUser, COUNT(f) FROM Follow f GROUP BY 1 ORDER BY 2 DESC")
    List<User> findAllByOrderByFollowCountDesc();

    // 검색 시 -> 검색 키워드와 일치하는 user 리스트를 나오게 한다.
    Page<User> findAllByNicknameContaining(String keyword, Pageable pageable);

    Optional<User> findByPhonenumber(String phonenumber);

    Optional<User> findByTo(String to);
}