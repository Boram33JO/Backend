package com.sparta.i_mu.global.errorCode;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    TOKEN_INVALID(4001 ,HttpStatus.BAD_REQUEST, "유효한 토큰이 아닙니다."),
    TOKEN_EXPIRED(4002, HttpStatus.BAD_REQUEST, "토큰이 만료되었습니다."),
    USER_NOT_AUTHENTICATED(4003, HttpStatus.BAD_REQUEST, "인증된 사용자가 아닙니다."),
    POST_NOT_EXIST(4004, HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    COMMENT_NOT_EXIST(4005, HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    USER_NOT_MATCH(4006, HttpStatus.BAD_REQUEST, "작성자만 수정, 삭제가 가능합니다."),
    FILE_INVALID(4007, HttpStatus.BAD_REQUEST, "유효한 파일이 아닙니다."),
    FILE_DECODE_FAIL(4008, HttpStatus.BAD_REQUEST, "파일 이름 디코딩에 실패했습니다."),
    URL_INVALID(4009, HttpStatus.BAD_REQUEST, "잘못된 URL 형식입니다."),
    EXTRACT_INVALID(40010, HttpStatus.BAD_REQUEST, "확장자를 추출할 수 없습니다."),
    BLACKLISTED(40011, HttpStatus.UNAUTHORIZED, "블랙리스트에 있는 토큰입니다."),
    REFRESH_TOKEN_INVALID(40012, HttpStatus.BAD_REQUEST, "REFRESH TOKEN 이 유효하지 않습니다."),
    REFRESH_TOKEN_MISMATCH(40013, HttpStatus.CONFLICT, "Redis에 저장된 REFRESH TOKEN과 동일하지 않습니다."),
    DATABASE_PROCESSING_ERROR(40014, HttpStatus.INTERNAL_SERVER_ERROR, "데이터를 deleted 처리하는 과정에서 오류가 발생했습니다."),
    USER_UNAUTHORIZED(40014, HttpStatus.UNAUTHORIZED, "로그인 후 사용이 가능합니다.");

    private final int errorCode;
    private final HttpStatus status;
    private final String message;

    ErrorCode(int errorCode, HttpStatus status, String message) {
        this.errorCode = errorCode;
        this.status = status;
        this.message = message;
    }
}
