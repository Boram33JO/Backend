package com.sparta.i_mu.global.handler;

import com.sparta.i_mu.global.exception.NoContentException;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;


@RestControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    // 일반적인 클라이언트의 잘못된 요청 시
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseResource<?> handleException(IllegalArgumentException e){
        return ResponseResource.error(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    //Validation 검증 실패 시
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseResource<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        BindingResult bindingResult = e.getBindingResult();

        StringBuilder builder = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            builder.append("[");
            builder.append(fieldError.getField());
            builder.append("](은)는 ");
            builder.append(fieldError.getDefaultMessage());
            builder.append(" 입력된 값: [");
            builder.append(fieldError.getRejectedValue());
            builder.append("]");
        }

        return ResponseResource.error(builder.toString(), HttpStatus.BAD_REQUEST);
    }

    // 사용자가 제출한 데이터로 해당 객체를 찾을 수 없을 때
    @ExceptionHandler(NullPointerException.class)
    public ResponseResource<?> handleException(NullPointerException e){
        return ResponseResource.error(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 권한 요청이 잘못들어왔을 경우
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseResource<?> handleException(AccessDeniedException e){
        return ResponseResource.error(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
    // 이미지 용량 초과
    @ExceptionHandler(SizeLimitExceededException.class)
    public ResponseResource<?> handleException(SizeLimitExceededException e){
        return ResponseResource.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // HTTP 204 status code -> Song 검색 시 콘텐츠가 존재하지 않습니다.
    @ExceptionHandler(NoContentException.class)
    public ResponseResource<?> handleNoContentException(NoContentException e) {
        return ResponseResource.error(e.getMessage(), HttpStatus.NO_CONTENT);
    }

}
