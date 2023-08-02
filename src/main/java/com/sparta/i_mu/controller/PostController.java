package com.sparta.i_mu.controller;

import com.sparta.i_mu.dto.requestDto.PostSaveRequestDto;
import com.sparta.i_mu.dto.requestDto.PostSearchRequestDto;
import com.sparta.i_mu.dto.responseDto.PostResponseDto;
import com.sparta.i_mu.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 현재 위치에 해당하는 노래 조회 (게시물에서 노래를 따로 빼야하나 -> songcontroller?)
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


    // 게시물 등록
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostSaveRequestDto postRequestDto) {
        return postService.createPost(postRequestDto);

    }
    // 위치 서비스에 따른 카테고리별 게시글 조회
    @Transactional(readOnly = true)
    @GetMapping
    public List<PostResponseDto> getPostByCategory(@RequestBody PostSearchRequestDto postSearchRequestDto){
        return postService.getPostByCategory(postSearchRequestDto);
    }
    // 게시물 수정
}
