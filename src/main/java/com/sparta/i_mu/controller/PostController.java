package com.sparta.i_mu.controller;

import com.sparta.i_mu.dto.requestDto.PostRequestDto;
import com.sparta.i_mu.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

// 현재 위치에 해당하는 노래 조회
// 주변 게시글 전체 조회
// 상세 게시글 조회
// 카테고리 별 게시글 조회
// 게시글 작성
// 게시글 수정
// 게시글 삭제
// 좋아요 순 게시글 조회
// 좋아요 순 반대 게시글 조회
// 최근 순 게시글 조회
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private PostService postService;

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostRequestDto postRequestDto){
     return postService.createPost(postRequestDto);

    }
}
