package com.sparta.i_mu.global.handler;

import com.sparta.i_mu.global.exception.NoContentException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> handleException(IllegalArgumentException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    //Validation 검증 실패 시
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
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

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(builder.toString());
    }

    // 사용자가 제출한 데이터로 해당 객체를 찾을 수 없을 때
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> handleException(NullPointerException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    // 권한 요청이 잘못들어왔을 경우
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleException(AccessDeniedException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }
    // 이미지 용량 초과
    @ExceptionHandler(SizeLimitExceededException.class)
    public ResponseEntity<String> handleException(SizeLimitExceededException e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    // HTTP 204 status code -> Song 검색 시 콘텐츠가 존재하지 않습니다.
    @ExceptionHandler(NoContentException.class)
    public ResponseEntity<?> handleNoContentException(NoContentException e) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
    }

}
