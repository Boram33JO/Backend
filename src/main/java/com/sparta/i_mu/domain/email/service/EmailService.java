package com.sparta.i_mu.domain.email.service;

import com.sparta.i_mu.domain.email.dto.EmailMessage;
import com.sparta.i_mu.domain.user.entity.User;
import com.sparta.i_mu.domain.user.repository.UserRepository;
import com.sparta.i_mu.global.exception.UserNotFoundException;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.util.RedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final RedisUtil redisUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public String sendEmail_sign(EmailMessage emailMessage, String email, String type) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            throw new UserNotFoundException("User with email " + email + " Already exist");
        }
        String authNum = createCode();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(emailMessage.getEmail()); //메일 수신자
            mimeMessageHelper.setSubject(emailMessage.getSubject()); // 메일 제목
            mimeMessageHelper.setText(authNum, true); // 메일 본문 내용, HTML 여부
            javaMailSender.send(mimeMessage); // 메일 본문 내용

            log.info("Success");

            redisUtil.setAuthNumData(email, authNum, 60 * 5L);

            return null;

        } catch (MessagingException e) {
            log.info("fail");
            throw new RuntimeException(e);
        }
    }

    public String sendEmail_pw(EmailMessage emailMessage, String email, String type) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (!optionalUser.isPresent()) {
            throw new UserNotFoundException("User with email " + email + " Already exist");
        }
        String authNum = createCode();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(emailMessage.getEmail()); //메일 수신자
            mimeMessageHelper.setSubject(emailMessage.getSubject()); // 메일 제목
            mimeMessageHelper.setText(authNum, true); // 메일 본문 내용, HTML 여부
            javaMailSender.send(mimeMessage); // 메일 본문 내용

            log.info("Success");

            redisUtil.setAuthNumData(email, authNum, 60 * 5L);

            return null;

        } catch (MessagingException e) {
            log.info("fail");
            throw new RuntimeException(e);
        }
    }


    // 인증번호 및 임시 비밀번호 생성 메서드
    public String createCode() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(4);

            switch (index) {
                case 0:
                    key.append((char) ((int) random.nextInt(26) + 97));
                    break;
                case 1:
                    key.append((char) ((int) random.nextInt(26) + 65));
                    break;
                default:
                    key.append(random.nextInt(9));
            }
        }
        return key.toString();
    }


    //인증코드 검증
    public Boolean verifyEmailCode(String email, String code) {
        String storedCode = redisUtil.getAuthNumData(email);
        if (storedCode != null && storedCode.equals(code)) {
            redisUtil.removeAuthNumData(email);  // 코드가 일치하면 코드를 제거합니다.
            return true;
        }
        return false;  // 코드가 일치하지 않거나 데이터가 없으면 false
    }


    //인증코드 검증 - 비밀번호를 찾을 때
    public Boolean verifyEmailCode_pw(String email, String code) {
        String storedCode = redisUtil.getAuthNumData(email);
        if (storedCode != null && storedCode.equals(code)) {
            return true;
        }
        return false;  // 코드가 일치하지 않거나 데이터가 없으면 false -> false 면 다음 창으로 넘어가질 수 없음
    }


    public ResponseResource<?> changePassword(String email, String newPassword, String code) {

        if (verifyEmailCode_pw(email, code)) {
            User user = userRepository.findByEmail(email).orElseThrow(()
                    -> new UserNotFoundException("User with email " + email + " not found"));

            // 새로운 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            userRepository.save(user);

            redisUtil.removeAuthNumData(email);

            return ResponseResource.message("비밀번호 변경이 완료되었습니다.", HttpStatus.OK);

        } else
            return ResponseResource.error("비밀번호 변경에 실패하였습니다.", HttpStatus.BAD_REQUEST.value());
    }

}
