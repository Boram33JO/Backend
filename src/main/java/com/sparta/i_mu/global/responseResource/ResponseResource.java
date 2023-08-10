package com.sparta.i_mu.global.responseResource;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseResource<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private int statusCode;
    private final ErrorCodeResponse error;

    public static <T> ResponseResource<T> data(T data, HttpStatus status){
        return ResponseResource.<T>builder()
                .success(true)
                .statusCode(status.value())
                .data(data)
                .build();
    }

    public static <T> ResponseResource<T> message(String message, HttpStatus status){
        return ResponseResource.<T>builder()
                .success(true)
                .statusCode(status.value())
                .message(message)
                .build();
    }

    public static <T> ResponseResource<T> error(ErrorCodeResponse errorResponse){
                return ResponseResource.<T>builder()
                .success(false)
                .error(errorResponse)
                .build();
    }
}
