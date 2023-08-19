package com.sparta.i_mu.controller;

import com.sparta.i_mu.dto.responseDto.SearchResponseDto;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.service.SearchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Tag(name = "Search", description = "Search API")
public class SearchController {

    private final SearchService searchService;
    // 메인 페이지 - 검색

    @GetMapping("/search")
    public ResponseEntity<ResponseResource<?>> getSearch(@RequestParam(value = "keyword") String keyword,
                                                         @RequestParam(value = "type") String type,
                                                         @RequestParam int page,
                                                         @RequestParam int size){
        Pageable pageable = PageRequest.of(page,size);
        if("all".equals(type)){
            SearchResponseDto result = searchService.getSearchAll(keyword);
            return ResponseEntity.status(HttpStatus.OK).body(ResponseResource.data(result, HttpStatus.OK, "전체 검색 결과입니다."));
        }
        Page<?> typeResult = searchService.getSearch(keyword,type,pageable);
        return ResponseEntity.ok(ResponseResource.data(typeResult,HttpStatus.OK, keyword + " 에 대한 검색 결과입니다. "));
    }
}
