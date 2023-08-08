package com.sparta.i_mu.kakao.repositoy;

import com.sparta.i_mu.kakao.entity.Kakao;
import org.springframework.data.jpa.repository.JpaRepository;


public interface KakaoRepository extends JpaRepository<Kakao, Long> {

    // JPA findBy 규칙
    // select * from user_master where kakao_email = ?
    public Kakao findByKakaoEmail(String kakaoEmail);

    Kakao findByKakaoId(Long kakaoId);
}