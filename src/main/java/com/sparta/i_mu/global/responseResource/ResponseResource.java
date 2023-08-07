package com.sparta.i_mu.global.responseResource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Builder
@AllArgsConstructor
public class ResponseResource<T> {
    private final boolean success;
    private final T data;
    private final ErrorCodeResponse error;

    private static <T> ResponseResource<T> error(ErrorCodeResponse errorResponse){
                return ResponseResource.<T>builder()
                .success(false)
                .data(null) //  에러는 null로 설정
                .error(errorResponse)
                .build();
    }
}
