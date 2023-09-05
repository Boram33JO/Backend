package com.sparta.i_mu.controller;


import com.sparta.i_mu.dto.requestDto.ChangePasswordRequest;
import com.sparta.i_mu.dto.requestDto.FindEmailRequestDto;
import com.sparta.i_mu.service.FindService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "revise", description = "비번 수정 API Document")
public class FindController {

    private final FindService findService;

    @PostMapping("/change-password")
    @Operation(summary = "잃어버린 비밀번호 수정", description = "잃어버린 비밀번호 수정")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        findService.changePassword(request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok("Password changed successfully");
    }


    @Operation(summary = "잃어버린 이메일 찾기", description = "잃어버린 이메일 찾기")
    @GetMapping("/find-email")
    public ResponseEntity<?> findemail(@RequestBody FindEmailRequestDto findEmailRequestDto) {
        String useremail = findService.findemail(findEmailRequestDto.getTo());
        return ResponseEntity.ok(useremail);
    }
}
