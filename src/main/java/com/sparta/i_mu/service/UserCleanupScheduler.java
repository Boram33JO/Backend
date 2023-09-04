package com.sparta.i_mu.service;

import com.sparta.i_mu.entity.Comment;
import com.sparta.i_mu.entity.Post;
import com.sparta.i_mu.entity.User;
import com.sparta.i_mu.repository.CommentRepository;
import com.sparta.i_mu.repository.PostRepository;
import com.sparta.i_mu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    @Scheduled(cron = "0 0 0 * * ?")
//    @Scheduled(cron = "0 */3 * * * ?")
    public void cleanupUsers() {
        LocalDateTime now = LocalDateTime.now();
        // 1. 삭제할 유저 리스트 조회
        List<User> usersToBeDeleted = userRepository.findAllByDeletedTrueAndDeleteAtBefore(now.minusMonths(1));
        log.info("삭제 될 유저의 리스트 : {}", usersToBeDeleted);

        // 2. 삭제할 유저의 ID 목록 추출
        List<Long> userIdsToBeDeleted = usersToBeDeleted.stream()
                .map(User::getId)
                .toList();
        // 3. 삭제할 유저의 게시글, 댓글 조회
        List<Post> postsToBeDeleted = postRepository.findAllByUserIdIn(userIdsToBeDeleted);
        List<Comment> commentsToBeDeleted = commentRepository.findAllByUserIdIn(userIdsToBeDeleted);

        // 4. 게시글 및 댓글 삭제
        postRepository.deleteAll(postsToBeDeleted);
        commentRepository.deleteAll(commentsToBeDeleted);

        // 5. 유저 삭제
        userRepository.deleteAll(usersToBeDeleted);
    }
}
