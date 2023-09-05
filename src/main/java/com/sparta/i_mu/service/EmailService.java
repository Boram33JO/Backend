package com.sparta.i_mu.service;

import com.sparta.i_mu.entity.EmailMessage;
import com.sparta.i_mu.entity.User;
import com.sparta.i_mu.global.util.RedisUtil;
import com.sparta.i_mu.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private  final RedisUtil redisUtil;
    private  final UserRepository userRepository;


    public String sendMail(EmailMessage emailMessage, String email, String type) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            throw new EmailService.UserNotFoundException("User with email " + email + " Already exist");
        }
        String authNum = createCode();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

//        if (type.equals("password")) userService.SetTempPassword(emailMessage.getTo(), authNum);

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(emailMessage.getTo()); //메일 수신자
            mimeMessageHelper.setSubject(emailMessage.getSubject()); // 메일 제목
            mimeMessageHelper.setText(authNum, true); // 메일 본문 내용, HTML 여부
            javaMailSender.send(mimeMessage); // 메일 본문 내용

            log.info("Success");

            redisUtil.setDataExpire(email, authNum, 60 * 5L);

            return null;

        } catch (MessagingException e) {
            log.info("fail");
            throw new RuntimeException(e);
        }
    }

    public String sendeMail(EmailMessage emailMessage, String email, String type) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (!optionalUser.isPresent()) {
            throw new EmailService.UserNotFoundException("User with email " + email + " Already exist");
        }
        String authNum = createCode();
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setTo(emailMessage.getTo()); //메일 수신자
            mimeMessageHelper.setSubject(emailMessage.getSubject()); // 메일 제목
            mimeMessageHelper.setText(authNum, true); // 메일 본문 내용, HTML 여부
            javaMailSender.send(mimeMessage); // 메일 본문 내용

            log.info("Success");

            redisUtil.setDataExpire(email, authNum, 60 * 5L);

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
                case 0: key.append((char) ((int) random.nextInt(26) + 97)); break;
                case 1: key.append((char) ((int) random.nextInt(26) + 65)); break;
                default: key.append(random.nextInt(9));
            }
        }
        return key.toString();
    }


    //인증코드 검증
    public Boolean verifyEmailCode(String email, String code) {
        String storedCode = redisUtil.getData(email);
        if (storedCode != null && storedCode.equals(code)) {
            redisUtil.removeData(email);  // 코드가 일치하면 코드를 제거합니다.
            return true;  
        }
        return false;  // 코드가 일치하지 않거나 데이터가 없으면 false
    }

    public class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}
