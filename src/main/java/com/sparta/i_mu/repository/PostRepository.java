package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.Category;
import com.sparta.i_mu.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 서브게시물 페이지 - 카테고리 (내 주변 전체)
    @Query("SELECT p FROM Post p WHERE ST_Distance_Sphere(Point(p.location.longitude, p.location.latitude), Point(:longitude, :latitude)) <= :DISTANCE_IN_METERS")
    List<Post> findAllByLocationNear(Double latitude, Double longitude, Double DISTANCE_IN_METERS);

    //메인 페이지 - 카테고리별 최신순
    List<Post> findAllByCategoryOrderByCreatedAtDesc(Category category);

    //서브 게시물 페이지 - 카테고리 별로 api 요청하는 조회
    List<Post> findAllPostByCategoryNameOrderByCreatedAtDesc(String category);

    // 메인페이지 - 좋아요 순을 기준으로 인기게시글 조회
    @Query("SELECT w.post, COUNT(w) AS c FROM Wishlist w GROUP BY w.post ORDER BY c DESC")
    List<Post> findAllByOrderByWishlistCountDesc();

    // 지도페이지 - 위치에 따른 카테고리별 조회
    @Query("SELECT p FROM Post p WHERE p.category.name = :name AND ST_Distance_Sphere(Point(p.location.longitude, p.location.latitude), Point(:longitude, :latitude)) <= :DISTANCE_IN_METERS")
    List<Post> findAllByCategoryAndLocationNear(@Param("name")String name, @Param("latitude") Double latitude, @Param("longitude") Double longitude, @Param("DISTANCE_IN_METERS") Double DISTANCE_IN_METERS);
}
