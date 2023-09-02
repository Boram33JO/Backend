package com.sparta.i_mu.repository.QueryDsl;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.i_mu.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class CustomPostRepositoryImpl implements CustomPostRepository {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 서브 게시글 조회 - 카테고리 별 전체 조회 기본(최신순)
     *
     * @param category
     * @param pageable
     * @return page/size 에 맞는 post 조회
     */
    @Override
    public Page<Post> findSubPostsByCategoryWithOrder(Long category, Pageable pageable) {
        QPost qPost = QPost.post;  // Q 클래스 인스턴스 생성

        List<Post> posts = jpaQueryFactory.selectFrom(qPost)
                .where(qPost.category.id.eq(category)
                        .and(qPost.deleted.eq(false)))
                .orderBy(getOrderSpecifiers(pageable.getSort()))
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
                .where(MySQLFunctions.stDistanceSphere(qPost.location.longitude, qPost.location.latitude, longitude, latitude).loe(DISTANCE_IN_METERS)
                        .and(qPost.deleted.eq(false)))
                .orderBy(getOrderSpecifiers(pageable.getSort()))
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
     * 동적 정렬문 -
     *
     * @param sort
     * @return
     */
    private OrderSpecifier[] getOrderSpecifiers(Sort sort) { // 반환 타입을 변경
        List<OrderSpecifier> orderSpecifiers = new ArrayList<>();

        sort.stream().forEach(order -> {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC; //정렬방향 결정
            String prop = order.getProperty(); // 정렬 대상의 필드
            log.info("prop : {}", prop);
            PathBuilder orderByExpression = new PathBuilder(Post.class, "post");
            orderSpecifiers.add(new OrderSpecifier<>(direction, orderByExpression.get(prop)));
            log.info("orderByExpression : {}", orderByExpression);
            log.info("orderSpecifiers : {}", orderSpecifiers);
        });

        return orderSpecifiers.toArray(new OrderSpecifier[0]); // 리스트를 배열로 변환하여 반환
    }

    /**
     * 메인 페이지 - 카테고리별 최신순으로 게시글 조회
     *
     * @param category
     * @return
     */
    @Override
    public List<Post> findMainPostsByCategory(Category category) {
        QPost qPost = QPost.post;
        return jpaQueryFactory
                .selectFrom(qPost)
                .where(qPost.deleted.isFalse() // deleted 필드가 false인 조건
                        .and(qPost.category.eq(category))) // 주어진 category와 일치하는 조건
                .orderBy(qPost.createdAt.desc()) // 생성 날짜를 기준으로 내림차순 정렬
                .fetch();
    }

    /**
     * 메인페이지 - 좋아요 순을 기준으로 인기 게시글 내림차순
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
                .orderBy(qWishlist.count().desc(), qPost.createdAt.desc()) //동일한 값은 최신순으로
                .fetch();
    }

    /**
     * 메인페이지 - 조회수 순을 기준으로 인기 게시글 내림차순
     * @return
     */
    public List<Post> findAllByOrderByViewCountDesc() {
        QPost qPost = QPost.post;

        return jpaQueryFactory
                .selectFrom(qPost)
                .where(qPost.deleted.eq(false))
                .orderBy(qPost.viewCount.desc(), qPost.createdAt.desc()) //동일한 값은 최신순으로
                .fetch();
    }


//    지도페이지 -> 위치 + 카테고리 별 조회


//    @Override
//    public Page<Post> findAllByCategoryAndLocationNear(Long categoryId, Double longitude, Double latitude, Double DISTANCE_IN_METERS, Pageable pageable) {
//        QPost qPost = QPost.post;
//        QCategory qCategory = QCategory.category;
//
//        List<Post> posts = jpaQueryFactory
//                .selectFrom(qPost)
//                .join(qPost.category, qCategory)
//                .where(qPost.deleted.eq(false)
//                        .and(qCategory.id.eq(categoryId))
//                        .and(MySQLFunctions.stDistanceSphere(qPost.location.longitude,qPost.location.latitude,longitude,latitude).loe(DISTANCE_IN_METERS)))
//                .orderBy(MySQLFunctions.stDistanceSphere(qPost.location.longitude,qPost.location.latitude,longitude,latitude).asc())
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//
//        long total = jpaQueryFactory
//                .selectFrom(qPost)
//                .join(qPost.category, qCategory)
//                .where(qPost.deleted.eq(false)
//                        .and(qCategory.id.eq(categoryId))
//                        .and(MySQLFunctions.stDistanceSphere(qPost.location.longitude,qPost.location.latitude,longitude,latitude).loe(DISTANCE_IN_METERS)))
//                .fetch().size();
//
//        return new PageImpl<>(posts, pageable, total);
//    }
//
//    /**
//     * 지도페이지 -> 위치 + 전체 게시글 조회
//     */
//    @Override
//    public Page<Post> findAllByLocationNear(Double longitude, Double latitude, Double DISTANCE_IN_METERS, Pageable pageable) {
//        QPost qPost = QPost.post;
//        QCategory qCategory = QCategory.category;
//
//        List<Post> posts = jpaQueryFactory
//                .selectFrom(qPost)
//                .join(qPost.category , qCategory)
//                .where(qPost.deleted.eq(false)
//                        .and(MySQLFunctions.stDistanceSphere(
//                                qPost.location.longitude,
//                                qPost.location.latitude,
//                                longitude, latitude).loe(DISTANCE_IN_METERS)))
//                .orderBy(MySQLFunctions.stDistanceSphere(qPost.location.longitude, qPost.location.latitude,longitude,latitude).asc())
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//
//        long total = jpaQueryFactory
//                .selectFrom(qPost)
//                .join(qPost.category, qCategory)
//                .where(qPost.deleted.eq(false)
//                        .and(MySQLFunctions.stDistanceSphere(qPost.location.longitude, qPost.location.latitude, longitude,latitude).loe(DISTANCE_IN_METERS)))
//                .fetch().size();
//
//        return new PageImpl<>(posts, pageable, total);
//    }

    // 조회수
//    @Override
//    public void viewCountUpdate(Long postId) {
//        QPost qPost = QPost.post;
//        jpaQueryFactory.update(qPost)
//                .set(qPost.viewCount, qPost.viewCount.add(1))
//                .where(qPost.id.eq(postId))
//                .execute();
//
//    };


    /**
     * 지도페이지 New Version
     * @param longitude
     * @param latitude
     * @param DISTANCE_IN_METERS
     * @param size
     * @return
     */
    @Override
    public List<Post> findAllByLocationNear(Double longitude, Double latitude, Double DISTANCE_IN_METERS, int size) {
        QPost qPost = QPost.post;

        return jpaQueryFactory
                .selectFrom(qPost)
                .where(qPost.deleted.eq(false)
                        .and(MySQLFunctions.stDistanceSphere(
                                qPost.location.longitude,
                                qPost.location.latitude,
                                longitude, latitude).loe(DISTANCE_IN_METERS)))
                .orderBy(MySQLFunctions.stDistanceSphere(qPost.location.longitude, qPost.location.latitude, longitude, latitude).asc())
                .limit(size)
                .fetch();
    }

}
