package com.sparta.i_mu.controller;

import com.sparta.i_mu.dto.requestDto.CommentRequestDto;
import com.sparta.i_mu.dto.responseDto.CommentResponseDto;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.security.UserDetailsImpl;
import com.sparta.i_mu.service.CommetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentController {

    private final CommetService commetService;

    @PostMapping("/posts/{postId}/comments")
    public ResponseResource<?> createComment(@PathVariable Long postId, @RequestBody CommentRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commetService.createComment(postId, requestDto, userDetails.getUser());
    }


    @PutMapping("/comments/{commentId}")
    public ResponseResource<?> updateComment(@PathVariable Long commentId, @RequestBody CommentRequestDto requestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commetService.updateComment(commentId, requestDto, userDetails.getUser());
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseResource<?> deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commetService.deleteComment(commentId, userDetails.getUser().getId());
    }

    @GetMapping("/profile/comments")
    public List<CommentResponseDto> getComment(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commetService.getComment(userDetails.getUser().getId());
    }

}
