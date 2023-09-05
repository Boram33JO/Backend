package com.sparta.i_mu.domain.post.repository;

import com.sparta.i_mu.domain.post.entity.Post;
import com.sparta.i_mu.domain.post.repository.QueryDsl.CustomPostRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    List<Post> findAllByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    // 조회수 쿼리
//    @Modifying
//    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
//    void viewCountUpdate(Long postId);

    // 조회수 비관적락, 베타락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :postId")
    Optional<Post> findByIdAndDeletedFalseForUpdate(Long postId);

    // 내가 쓴 포스팅 조회
    Page<Post> findAllByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    /**
     *
     * @param userId
     * @return
     */
    List<Post> findAllByUserIdAndDeletedFalse(Long userId);

    /**
     * 탈퇴한 유저의 일정 시간이후 게시글을 자동삭제 되게 하기 위해 해당 유저의 아이디로 조회
     * @param userIdsToBeDeleted
     * @return
     */
    List<Post> findAllByUserIdIn(List<Long> userIdsToBeDeleted);
}