package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    boolean existsByNickname(String Nickname);
}
