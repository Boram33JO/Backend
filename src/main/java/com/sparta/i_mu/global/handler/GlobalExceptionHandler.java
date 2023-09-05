package com.sparta.i_mu.global.handler;

import com.sparta.i_mu.global.exception.NoContentException;
import com.sparta.i_mu.global.exception.UserNotFoundException;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.*;

@Slf4j
@RestControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    // 일반적인 클라이언트의 잘못된 요청 시
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResource<?> handleException(IllegalArgumentException e){
        return ResponseResource.error(e.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    //Validation 검증 실패 시
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResource<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){

        BindingResult bindingResult = e.getBindingResult();

        List<String> errorList = new ArrayList<>();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errorList.add(fieldError.getDefaultMessage());
        }

        Collections.sort(errorList);
        String errorString = String.join("\n", errorList);

        return ResponseResource.error(errorString, HttpStatus.BAD_REQUEST.value());
    }

    // 사용자가 제출한 데이터로 해당 객체를 찾을 수 없을 때
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseResource<?> handleException(NullPointerException e){
        return ResponseResource.error(e.getMessage(), HttpStatus.NOT_FOUND.value());
    }

    // 권한 요청이 잘못들어왔을 경우
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseResource<?> handleException(AccessDeniedException e){
        return ResponseResource.error(e.getMessage(), HttpStatus.UNAUTHORIZED.value());
    }
    // 이미지 용량 초과
    @ExceptionHandler(SizeLimitExceededException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseResource<?> handleException(SizeLimitExceededException e){
        return ResponseResource.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    // HTTP 204 status code -> Song 검색 시 콘텐츠가 존재하지 않습니다.
    @ExceptionHandler(NoContentException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseResource<?> handleNoContentException(NoContentException e) {
        return ResponseResource.error(e.getMessage(), HttpStatus.NO_CONTENT.value());
    }

    // 이메일, 문자 인증
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseResource<?> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseResource.error(e.getMessage(), HttpStatus.NOT_FOUND.value());
    }

}
