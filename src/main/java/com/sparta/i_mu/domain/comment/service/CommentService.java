package com.sparta.i_mu.domain.comment.service;

import com.sparta.i_mu.domain.comment.dto.CommentRequestDto;
import com.sparta.i_mu.domain.comment.dto.CommentResponseDto;
import com.sparta.i_mu.domain.comment.entity.Comment;
import com.sparta.i_mu.domain.post.entity.Post;
import com.sparta.i_mu.domain.user.entity.User;
import com.sparta.i_mu.global.errorCode.ErrorCode;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.util.NotificationType;
import com.sparta.i_mu.domain.comment.repository.CommentRepository;
import com.sparta.i_mu.domain.post.repository.PostRepository;
import com.sparta.i_mu.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    public ResponseResource<?> createComment(Long postId, CommentRequestDto requestDto, User user) {
        Post post = findPost(postId);

        Comment comment = Comment.builder()
                .user(user)
                .content(requestDto.getContent())
                .post(post)
                .deleted(false)
                .build();

        commentRepository.save(comment);
        notificationService.send(post.getUser(), NotificationType.COMMENT , "게시글에 댓글이 작성 되었습니다.", "/posts/" + postId);

        return ResponseResource.message("댓글 등록 성공", HttpStatus.OK);
    }

    public Page<CommentResponseDto> getComment(Long postId, Pageable pageable) {

        Page<CommentResponseDto> comments = commentRepository.findAllByPostIdAndDeletedFalse(postId, pageable).map(CommentResponseDto::new);

        return comments;
    }

    @Transactional
    public ResponseResource<?> updateComment(Long commentId, CommentRequestDto requestDto, User user) {
        Comment comment = findComment(commentId);

        checkUser(comment.getUser().getId(), user.getId());

        comment.update(requestDto);

        return ResponseResource.message("댓글 수정 성공", HttpStatus.OK);
    }

    @Transactional
    public ResponseResource<?> deleteComment(Long commentId, Long userId) {
        Comment comment = findComment(commentId);

        checkUser(comment.getUser().getId(), userId);

        comment.setDeleted(true);
        comment.setDeletedAt(LocalDateTime.now());

        return ResponseResource.message("댓글 삭제 성공", HttpStatus.OK);
    }


    private Post findPost (Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException(ErrorCode.POST_NOT_EXIST.getMessage()));
    }

    private Comment findComment (Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException(ErrorCode.COMMENT_NOT_EXIST.getMessage()));
    }

    private void checkUser (Long commentUserId, Long loginUserId) {
        if (!Objects.equals(commentUserId, loginUserId)) {
            throw new IllegalArgumentException(ErrorCode.USER_NOT_MATCH.getMessage());
        }
    }

}
