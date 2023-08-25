package com.sparta.i_mu.global.responseResource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.i_mu.global.errorCode.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseResource<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final int statusCode;
    private final String error;
    private final ErrorCode errorCode;

    public static <T> ResponseResource<T> data(T data, HttpStatus status, String message){
        return ResponseResource.<T>builder()
                .success(true)
                .statusCode(status.value())
                .data(data)
                .message(message)
                .build();
    }

    public static <T> ResponseResource<T> message(String message, HttpStatus status){
        return ResponseResource.<T>builder()
                .success(true)
                .statusCode(status.value())
                .message(message)
                .build();
    }

    // 1안
//    public static <T> ResponseResource<T> error1(ErrorCodeResponse errorResponse){
//                return ResponseResource.<T>builder()
//                .success(false)
//                .error1(errorResponse)
//                .build();
//    }

    // 2안
    public static <T> ResponseResource<T> error(String message, HttpStatus status){
        return ResponseResource.<T>builder()
                .success(false)
                .statusCode(status.value())
                .error(message)
                .build();
    }
}
