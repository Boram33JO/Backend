package com.sparta.i_mu.controller;

import com.sparta.i_mu.dto.requestDto.PostSaveRequestDto;
import com.sparta.i_mu.dto.requestDto.PostSearchRequestDto;
import com.sparta.i_mu.dto.responseDto.PostResponseDto;
import com.sparta.i_mu.entity.User;
import com.sparta.i_mu.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

// 1. 게시글 작성 -> O
// 2. 게시글 수정 -> O
// 3. 게시글 삭제 -> O
// 4. 상세 게시글 조회 -> O
// 5. 위치 서비스에 따른 전체 게시글 조회 ->
// . 위치 서비스에 따른 카테고리별 게시글 조회 -> O
// . 지도페이지에서 검색시 주변 게시글 조회
// . 전국 기준 좋아요 순 인기 게시글 조회 -> O
// . 최신 순 게시글 조회
// . 작성 순 게시글 조회

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


    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @RequestBody PostSaveRequestDto postRequestDto
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws AccessDeniedException {

        User user = userDetails.getUser();
        return postService.updatePost(postId,postRequestDto,user);
    }

    // 게시물 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws AccessDeniedException {
        User user = userDetails.getUser();

        return postService.deletePost(postId,user);
    }

    // 4. 상세 게시글 조회
    @GetMapping("/{postId}")
    public PostResponseDto getDetailPost(@PathVariable Long postId){
        return postService.getDetailPost(postId);
    }

//위치 서비스에 따른 전체 게시글 조회
    @GetMapping
    public List<PostResponseDto> getAllPost(@RequestBody PostSearchRequestDto postSearchRequestDto){
        return postService.getAllPost(postSearchRequestDto);
    }

    // 5. 위치 서비스에 따른 카테고리별 게시글 조회

    @GetMapping
    public List<?> getPostByCategory(
            @RequestBody PostSearchRequestDto postSearchRequestDto){
        return postService.getPostByCategory(postSearchRequestDto);
    }

    // 전국 기준 좋아요 순 인기 게시글 조회
    @GetMapping("/wishlist")
    public List<PostResponseDto> getPostByWishlist(){
        return postService.getPostByWishlist();
    }



}
