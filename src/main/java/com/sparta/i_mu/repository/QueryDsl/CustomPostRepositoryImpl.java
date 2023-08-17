package com.sparta.i_mu.repository.QueryDsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.i_mu.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomPostRepositoryImpl implements CustomPostRepository {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 서브 게시글 조회 - 카테고리 별 전체 조회 기본(최신순)
     * @param category
     * @param pageable
     * @return page/size 에 맞는 post 조회
     */
    @Override
    public Page <Post> findSubPostsByCategoryWithOrder(Long category, Pageable pageable) {
        QPost qPost = QPost.post;  // Q 클래스 인스턴스 생성

        List<Post> posts = jpaQueryFactory.selectFrom(qPost)
                .where(qPost.category.id.eq(category)
                        .and(qPost.deleted.eq(false)))
                .orderBy(qPost.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = jpaQueryFactory.selectFrom(qPost)
                .where(qPost.category.id.eq(category)
                        .and(qPost.deleted.eq(false)))
                .fetch().size();

        return new PageImpl<>(posts, pageable, total);
    }
    /**
     * 서브 게시글 리스트 조회 - 내 주변
     */
    @Override
    public Page<Post> findAllByLocationNearOrderByCreatedAtDesc(Double longitude, Double latitude, Double DISTANCE_IN_METERS, Pageable pageable) {
        QPost qPost = QPost.post;
        List<Post> posts = jpaQueryFactory
                .selectFrom(qPost)
                .where(MySQLFunctions.stDistanceSphere(qPost.location.longitude, qPost.location.latitude,  longitude, latitude).loe(DISTANCE_IN_METERS)
                        .and(qPost.deleted.eq(false)))
                .orderBy(qPost.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 총 게시물 수 조회
        long total = jpaQueryFactory
                .selectFrom(qPost)
                .where(MySQLFunctions.stDistanceSphere(qPost.location.longitude, qPost.location.latitude, longitude, latitude).loe(DISTANCE_IN_METERS)
                        .and(qPost.deleted.eq(false)))
                .fetch().size();

        return new PageImpl<>(posts, pageable, total);
    }
    /**
     * 메인 페이지 - 카테고리별 최신순으로 게시글 조회
     * @param category
     * @return
     */
    @Override
    public List<Post> findMainPostsByCategory(Category category) {
        QPost qPost = QPost.post;
        return  jpaQueryFactory
                .selectFrom(qPost)
                .where(qPost.deleted.isFalse() // deleted 필드가 false인 조건
                        .and(qPost.category.eq(category))) // 주어진 category와 일치하는 조건
                .orderBy(qPost.createdAt.desc()) // 생성 날짜를 기준으로 내림차순 정렬
                .fetch();
    }

    /**
     * 메인페이지 - 좋아요 순을 기준으로 인기 게시글 내림차순 조회 -> 조회수로 바뀔 부분
     * @return
     */
    public List<Post> findAllByOrderByWishlistCountDesc() {
        QPost qPost = QPost.post;
        QWishlist qWishlist = QWishlist.wishlist;

        return jpaQueryFactory
                .selectFrom(qPost)
                .join(qWishlist).on(qPost.id.eq(qWishlist.post.id))
                .where(qPost.deleted.eq(false))
                .groupBy(qPost) //그룹화 한 후 개수조회
                .orderBy(qWishlist.count().desc(),qPost.createdAt.desc()) //동일한 값은 최신순으로
                .fetch();
    }



//    지도페이지 -> 위치 + 카테고리 별 조회
    @Override
    public Page<Post> findAllByCategoryAndLocationNear(Long categoryId, Double longitude, Double latitude, Double DISTANCE_IN_METERS, Pageable pageable) {
        QPost qPost = QPost.post;
        QCategory qCategory = QCategory.category;

        List<Post> posts = jpaQueryFactory
                .selectFrom(qPost)
                .join(qPost.category, qCategory)
                .where(qPost.deleted.eq(false)
                        .and(qCategory.id.eq(categoryId))
                        .and(MySQLFunctions.stDistanceSphere(qPost.location.longitude,qPost.location.latitude,longitude,latitude).loe(DISTANCE_IN_METERS)))
                .orderBy(MySQLFunctions.stDistanceSphere(qPost.location.longitude,qPost.location.latitude,longitude,latitude).asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = jpaQueryFactory
                .selectFrom(qPost)
                .join(qPost.category, qCategory)
                .where(qPost.deleted.eq(false)
                        .and(qCategory.id.eq(categoryId))
                        .and(MySQLFunctions.stDistanceSphere(qPost.location.longitude,qPost.location.latitude,longitude,latitude).loe(DISTANCE_IN_METERS)))
                .fetch().size();

        return new PageImpl<>(posts, pageable, total);
    }

    /**
     * 지도페이지 -> 위치 + 전체 게시글 조회
     */
    @Override
    public Page<Post> findAllByLocationNear(Double longitude, Double latitude, Double DISTANCE_IN_METERS, Pageable pageable) {
        QPost qPost = QPost.post;
        QCategory qCategory = QCategory.category;

        List<Post> posts = jpaQueryFactory
                .selectFrom(qPost)
                .join(qPost.category , qCategory)
                .where(qPost.deleted.eq(false)
                        .and(MySQLFunctions.stDistanceSphere(
                                qPost.location.longitude,
                                qPost.location.latitude,
                                longitude, latitude).loe(DISTANCE_IN_METERS)))
                .orderBy(MySQLFunctions.stDistanceSphere(qPost.location.longitude, qPost.location.latitude,longitude,latitude).asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = jpaQueryFactory
                .selectFrom(qPost)
                .join(qPost.category, qCategory)
                .where(qPost.deleted.eq(false)
                        .and(MySQLFunctions.stDistanceSphere(qPost.location.longitude, qPost.location.latitude, longitude,latitude).loe(DISTANCE_IN_METERS)))
                .fetch().size();

        return new PageImpl<>(posts, pageable, total);
    }

}