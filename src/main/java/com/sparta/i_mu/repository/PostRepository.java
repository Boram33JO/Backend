package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, CustomPostRepository {


    Optional<Post> findByIdAndDeletedFalse(Long postId);

    // 서브 게시물 페이지 - 카테고리 (내 주변 전체) - 최신순 기준
    @Query("SELECT p FROM Post p WHERE ST_Distance_Sphere(Point(p.location.longitude, p.location.latitude)," +
            " Point(:longitude, :latitude)) <= :DISTANCE_IN_METERS")
    Page<Post> findAllByLocationNearOrderByCreatedAtDesc(@Param("latitude") Double latitude,
                                                         @Param("longitude") Double longitude,
                                                         @Param("DISTANCE_IN_METERS") Double DISTANCE_IN_METERS,
                                                         Pageable pageable);


    // 지도페이지 - 위치에 따른 카테고리별 조회
    @Query("SELECT p FROM Post p WHERE p.category.name = :name " +
            "AND ST_Distance_Sphere(Point(p.location.longitude, p.location.latitude)," +
            "Point(:longitude, :latitude)) <= :DISTANCE_IN_METERS " +
            "order by ST_Distance_Sphere(Point(p.location.longitude, p.location.latitude)," +
            "Point(:longitude, :latitude))")
    Page<Post> findAllByCategoryAndLocationNear(@Param("name") String name,
                                                @Param("latitude") Double latitude,
                                                @Param("longitude") Double longitude,
                                                @Param("DISTANCE_IN_METERS") Double DISTANCE_IN_METERS,
                                                Pageable pageable);

    // 지도 페이지 - categoryId가 없을 때
    @Query("SELECT p FROM Post p WHERE ST_Distance_Sphere(Point(p.location.longitude, p.location.latitude)," +
            " Point(:longitude, :latitude)) <= :DISTANCE_IN_METERS" +
            " order by ST_Distance_Sphere(Point(p.location.longitude, p.location.latitude)," +
            " Point(:longitude, :latitude))")
    Page<Post> findAllByLocationNear(@Param("latitude") Double latitude,
                                     @Param("longitude") Double longitude,
                                     @Param("DISTANCE_IN_METERS") Double DISTANCE_IN_METERS,
                                     Pageable pageable);

    List<Post> findAllByUserId(Long userId);


    /**
     * 해당 키워드가 제목에 포함하는 게시글 리스트를 조회
     * @param keyword
     * @param pageable
     * @return 게시글 리스트 pageable
     */
    Page<Post> findAllByPostTitleContainingAndDeletedFalse(String keyword, Pageable pageable);

    /**
     * 해당 키워드가 주소에 포함하는 게시글 리스트를 조회
     * @param keyword
     * @param pageable
     * @return 게시글 리스트 pageable
     */
    Page<Post> findAllByLocationAddressContainingAndDeletedFalse(String keyword, Pageable pageable);

}