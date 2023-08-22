package com.sparta.i_mu.controller;

import com.sparta.i_mu.dto.responseDto.SearchResponseDto;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Tag(name = "Search", description = "검색 API Document")
public class SearchController {

    private final SearchService searchService;
    // 메인 페이지 - 검색

    @GetMapping("/search")
    @Operation(summary = "메인 페이지 검색", description = "메인 페이지 검색")
    public ResponseEntity<ResponseResource<?>> getSearch(@RequestParam(value = "keyword") String keyword,
                                                         @RequestParam(value = "type") String type,
                                                         @RequestParam int page,
                                                         @RequestParam int size,
                                                         @RequestParam String sortBy){

        Sort sort = getSortByParameter(sortBy,type);
        Pageable pageable = PageRequest.of(page,size,sort);
        if("all".equals(type)){
            SearchResponseDto result = searchService.getSearchAll(keyword,pageable);
            return ResponseEntity.status(HttpStatus.OK).body(ResponseResource.data(result, HttpStatus.OK, "전체 검색 결과입니다."));
        }
        Page<?> typeResult = searchService.getSearch(keyword,type,pageable);
        return ResponseEntity.ok(ResponseResource.data(typeResult,HttpStatus.OK, keyword + " 에 대한 검색 결과입니다. "));
    }

    private Sort getSortByParameter(String sortBy, String type) {
        if ("songName".equals(type)||"nickname".equals(type)){
            return Sort.unsorted();
        }
        return switch (sortBy) {
            case "wishlist" -> Sort.by(Sort.Direction.DESC, "wishlistCount");
            case "views" -> Sort.by(Sort.Direction.DESC, "viewCount");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
