package com.sparta.i_mu.controller;

import com.sparta.i_mu.dto.requestDto.MapPostSearchRequestDto;
import com.sparta.i_mu.dto.requestDto.PostSaveRequestDto;
import com.sparta.i_mu.dto.responseDto.PostByCategoryResponseDto;
import com.sparta.i_mu.dto.responseDto.PostResponseDto;
import com.sparta.i_mu.entity.User;
import com.sparta.i_mu.security.UserDetailsImpl;
import com.sparta.i_mu.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Tag(name = "Post", description = "게시글 API Document")
public class PostController {
    private final PostService postService;

    // 게시물 등록
    @PostMapping
    @Operation(summary = "게시글 작성", description = "게시글 작성")
    public ResponseEntity<?> createPost(
            @RequestBody PostSaveRequestDto postRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        return postService.createPost(postRequestDto, user);
    }


    // 게시글 수정
    @PutMapping("/{postId}")
    @Operation(summary = "게시글 수정", description = "게시글 수정")
    @Parameter(name = "postId", description = "수정할 게시글의 ID ")
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @RequestBody PostSaveRequestDto postRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws AccessDeniedException {

        User user = userDetails.getUser();
        return postService.updatePost(postId, postRequestDto, user);
    }

    // 게시물 삭제
    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제", description = "게시글 삭제")
    @Parameter(name = "postId", description = "삭제할 게시글의 ID ")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws AccessDeniedException {
        User user = userDetails.getUser();

        return postService.deletePost(postId, user);
    }

    // 메인페이지 - 카테고리 별 전체 게시글 조회
    @GetMapping
    @Operation(summary = "카테고리 별 전체 게시글 조회", description = "카테고리 별 전체 게시글 조회")
    public List<PostByCategoryResponseDto> getAllPost() {
        return postService.getAllPost();
    }

    // 상세 페이지 - 상세 게시글 조회
    @GetMapping("/{postId}")
    @Operation(summary = "상세 게시글 조회", description = "상세 게시글 조회")
    @Parameter(name = "postId", description = "조회할 게시글의 ID ")
    public PostResponseDto getDetailPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest req,
            HttpServletResponse res) {
        return postService.getDetailPost(postId, Optional.ofNullable(userDetails), req, res);
    }

    // 상세 리스트 페이지 - 내주변
    @GetMapping("/area")
    @Operation(summary = "내 주변 게시물 리스트 조회", description = "내 주변 게시물 리스트 조회")
    public Page<PostResponseDto> getAllAreaPost(
            @RequestBody MapPostSearchRequestDto postSearchRequestDto,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction) {
        Sort sort = Sort.unsorted();
        if (sortBy != null && direction != null) {
            sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        }
        Pageable pageable = PageRequest.of(page, size, sort);
        return postService.getAllAreaPost(postSearchRequestDto, pageable);

    }

    // 서브 리스트 페이지 - 카테고리별
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "카테고리별 게시물 리스트 조회", description = "카테고리별 게시물 리스트 조회")
    @Parameter(name = "categoryId", description = "조회할 카테고리의 ID ")
    public Page<PostResponseDto> getPostByCategory(
            @PathVariable Long categoryId,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction) {
        Sort sort = Sort.unsorted();
        if (sortBy != null && direction != null) {
            sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        }
        Pageable pageable = PageRequest.of(page, size, sort);
        return postService.getPostByCategory(categoryId, pageable);

    }

    // 지도페이지 - 위치 서비스에 따른 카테고리별 게시글 조회
    @PostMapping("/map")
    @Operation(summary = "위치 서비스에 따른 카테고리별 게시글 조회", description = "위치 서비스에 따른 카테고리별 게시글 조회")
    public Page<PostResponseDto> getMapPostByCategory(
            @RequestBody MapPostSearchRequestDto postSearchRequestDto,
            @RequestParam(required = false) Optional<Long> categoryId,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String direction) {
        Sort sort = Sort.unsorted();
        if (sortBy != null && direction != null) {
            sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        }
        Pageable pageable = PageRequest.of(page, size, sort);
        return postService.getMapPostByCategory(postSearchRequestDto, categoryId, pageable);

    }

    // 전국 기준 좋아요 순 인기 게시글 조회
    @GetMapping("/wishlist")
    @Operation(summary = "전국 기준 좋아요 순 인기 게시글 조회", description = "전국 기준 좋아요 순 인기 게시글 조회")
    public List<PostResponseDto> getPostByWishlist() {
        return postService.getPostByWishlist();
    }
}
