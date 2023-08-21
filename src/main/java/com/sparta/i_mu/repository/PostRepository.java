package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.Post;
import com.sparta.i_mu.repository.QueryDsl.CustomPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, CustomPostRepository {


    Optional<Post> findByIdAndDeletedFalse(Long postId);

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

    List<Post> findAllByUserIdAndDeletedFalse(Long userId);

    // 조회수 쿼리
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void viewCountUpdate(Long postId);

}