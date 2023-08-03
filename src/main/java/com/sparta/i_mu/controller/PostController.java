package com.sparta.i_mu.controller;

import com.sparta.i_mu.dto.requestDto.PostSaveRequestDto;
import com.sparta.i_mu.dto.requestDto.PostSearchRequestDto;
import com.sparta.i_mu.dto.responseDto.PostResponseDto;
import com.sparta.i_mu.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 주변 게시글 카테고리별 전체 조회 -> O
// 상세 게시글 조회
// 게시글 작성 -> O
// 게시글 수정 -> O
// 게시글 삭제 -> O
// 좋아요 순 게시글 조회
// 좋아요 순 반대 게시글 조회
// 최근 순 게시글 조회

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private PostService postService;


    // 게시물 등록
    @PostMapping
    public ResponseEntity<?> createPost(
            @RequestBody PostSaveRequestDto postRequestDto
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        return postService.createPost(postRequestDto,user);
    }

    // 상세 게시글 조회
    @GetMapping("/{postId}")
    public PostResponseDto getDetailPost(@PathVariable Long postId){
        return postService.getDetailPost(postId);
    }



    @GetMapping("/")
    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @RequestBody PostSaveRequestDto postRequestDto
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        User user = userDetails.getUser();
        return postService.updatePost(postId,postRequestDto,user);
    }

    // 게시물 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails){
        User user = userDetails.getUser();

        return postService.deletePost(postId,user);
    }


    // 위치 서비스에 따른 카테고리별 게시글 조회
    @Transactional(readOnly = true)
    @GetMapping
    public List<?> getPostByCategory(
            @RequestBody PostSearchRequestDto postSearchRequestDto){
        return postService.getPostByCategory(postSearchRequestDto);
    }

}
