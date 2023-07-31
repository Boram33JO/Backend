package com.sparta.i_mu.global.responseResource;

import com.nimbusds.oauth2.sdk.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Builder
@AllArgsConstructor(access = PROTECTED)
public class ResponseResource<T> {
    private final boolean success;
    private final T data;
    private final ErrorResponse error;

    private static <T> ResponseResource<T> error(ErrorResponse errorResponse){
                return ResponseResource.<T>builder()
                .success(false)
                .data(null) //  에러는 null로 설정
                .error(errorResponse)
                .build();
    }
}
