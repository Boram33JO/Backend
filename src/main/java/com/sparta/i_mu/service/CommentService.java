package com.sparta.i_mu.service;

import com.sparta.i_mu.dto.requestDto.CommentRequestDto;
import com.sparta.i_mu.dto.responseDto.CommentResponseDto;
import com.sparta.i_mu.entity.Comment;
import com.sparta.i_mu.entity.Post;
import com.sparta.i_mu.entity.User;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.repository.CommentRepository;
import com.sparta.i_mu.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public ResponseResource<?> createComment(Long postId, CommentRequestDto requestDto, User user) {
        Post post = findPost(postId);

        Comment comment = Comment.builder()
                .user(user)
                .content(requestDto.getContent())
                .post(post)
                .build();

        commentRepository.save(comment);

        return new ResponseResource<>(true, "댓글 등록 성공", null);
    }

    @Transactional
    public ResponseResource<?> updateComment(Long commentId, CommentRequestDto requestDto, User user) {
        Comment comment = findComment(commentId);

        checkUser(comment.getUser().getId(), user.getId());

        comment.update(requestDto);

        return new ResponseResource<>(true, "댓글 수정 성공", null);
    }

    public ResponseResource<?> deleteComment(Long commentId, Long userId) {
        Comment comment = findComment(commentId);

        checkUser(comment.getUser().getId(), userId);

        commentRepository.delete(comment);

        return new ResponseResource<>(true, "댓글 삭제 성공", null);
    }

    public List<CommentResponseDto> getComment(Long userId) {
        List<CommentResponseDto> commentList = commentRepository.findAllByUserId(userId).stream().map(CommentResponseDto::new).toList();

        return commentList;
    }

    private Post findPost (Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다."));
    }

    private Comment findComment (Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));
    }

    private void checkUser (Long commentUserId, Long loginUserId) {
        if (!Objects.equals(commentUserId, loginUserId)) {
            throw new IllegalArgumentException("작성자만 삭제/수정 할 수 있습니다.");
        }
    }

}
