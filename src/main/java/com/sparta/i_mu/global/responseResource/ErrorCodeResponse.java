package com.sparta.i_mu.global.responseResource;

import com.sparta.i_mu.global.errorCode.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorCodeResponse {
    private int ErrorCode;
    private String message;
    private HttpStatus status;

    public ErrorCodeResponse(ErrorCode errorCode) {
        this.ErrorCode = errorCode.getErrorCode();
        this.message = errorCode.getMessage();
        this.status = errorCode.getStatus();
    }

}

