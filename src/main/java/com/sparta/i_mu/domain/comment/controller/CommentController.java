package com.sparta.i_mu.domain.comment.controller;

import com.sparta.i_mu.domain.comment.dto.CommentRequestDto;
import com.sparta.i_mu.domain.comment.dto.CommentResponseDto;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.security.UserDetailsImpl;
import com.sparta.i_mu.domain.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Comment", description = "댓글 API Document")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "댓글 작성", description = "게시글에 댓글 작성")
    @Parameter(name = "postId", description = "작성할 댓글의 게시판 ID ")
    public ResponseResource<?> createComment(@PathVariable Long postId, @RequestBody CommentRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commentService.createComment(postId, requestDto, userDetails.getUser());
    }

    @GetMapping("/posts/{postId}/comments")
    @Operation(summary = "댓글 조회", description = "게시글에 댓글 작성")
    @Parameter(name = "postId", description = "조회 댓글의 게시판 ID ")
    public Page<CommentResponseDto> getComment(@PathVariable Long postId, Pageable pageable) {
        return commentService.getComment(postId, pageable);
    }


    @PutMapping("/comments/{commentId}")
    @Operation(summary = "댓글 수정", description = "게시글에 작성한 댓글 수정")
    @Parameter(name = "commentId", description = "수정할 댓글의 ID ")
    public ResponseResource<?> updateComment(@PathVariable Long commentId, @RequestBody CommentRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commentService.updateComment(commentId, requestDto, userDetails.getUser());
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제", description = "게시글에 작성한 댓글 삭제")
    @Parameter(name = "commentId", description = "삭제할 댓글의 ID ")
    public ResponseResource<?> deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commentService.deleteComment(commentId, userDetails.getUser().getId());
    }

}
