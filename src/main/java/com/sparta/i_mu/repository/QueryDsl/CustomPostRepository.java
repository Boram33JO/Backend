package com.sparta.i_mu.repository.QueryDsl;

import com.sparta.i_mu.entity.Category;
import com.sparta.i_mu.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Point;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomPostRepository {

    //서브 게시글 조회 - 카테고리 별 전체 조회 기본 (최신순)
    Page <Post> findSubPostsByCategoryWithOrder(Long category, Pageable pageable);

    /**
     * 메인 페이지 - 카테고리별 최신순
     * @param category
     * @return 카테고리별로 최신순으로 post의 list를 반환.
     */
    List<Post> findMainPostsByCategory(Category category);


    /**
     * 메인 페이지 - 좋아요 순을 기준으로 인기 게시글 조회
     * @return
     */
    List<Post> findAllByOrderByWishlistCountDesc();

    /**
     * 메인 페이지 - 조회수 순을 기준으로 인기 게시글 조회
     * @return
     */
    List<Post> findAllByOrderByViewCountDesc();

    /**
     * 상세 게시물 페이지 내 주변
     */
    Page<Post> findAllByLocationNearOrderByCreatedAtDesc(Double longitude, Double latitude,  Double DISTANCE_IN_METERS, Pageable pageable);


    /**
     * 지도페이지 - 위치에 따른 카테고리별 조회
     */

//    Page<Post> findAllByCategoryAndLocationNear(Long categoryId, Double latitude, Double longitude, Double DISTANCE_IN_METERS, Pageable pageable);

    /**
     * 지도페이지 - 전체 조회
     * @return
     */
//    Page<Post> findAllByLocationNear(Double latitude,
//                                     Double longitude,
//                                     @Param("DISTANCE_IN_METERS") Double DISTANCE_IN_METERS,
//                                     Pageable pageable);

    //**

    List<Post> findAllByLocationNear(Double latitude,
                                     Double longitude,
                                     Double DISTANCE_IN_METERS,
                                     int size);


    // 조회수
//    void viewCountUpdate(Long postId);

}
