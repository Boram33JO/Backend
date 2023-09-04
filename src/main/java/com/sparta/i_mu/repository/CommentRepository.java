package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId);

    Page<Comment> findAllByUserIdAndDeletedFalse(Long userId, Pageable pageable);



    Page<Comment> findAllByPostIdAndDeletedFalse(Long id, Pageable pageable);


    // 회원 탈퇴

    /**
     * 회원 탈퇴한 유저가 일정 시간이 지났을 때 아예 삭제처리를 위한 처리
     * @param userIdsToBeDeleted
     * @return
     */
    List<Comment> findAllByUserIdIn(List<Long> userIdsToBeDeleted);

    /**
     * 회원 탈퇴 시 찾는 용도
     * @param userId
     * @return
     */
    List<Comment> findAllByUserIdAndDeletedFalse(Long userId);
}
