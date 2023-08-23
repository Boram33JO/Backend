//package com.sparta.i_mu.service;
//
//import com.querydsl.core.support.Context;
//import com.sparta.i_mu.dto.requestDto.EmailMessageRequestDto;
//import com.sparta.i_mu.dto.requestDto.EmailPostDto;
//import com.sparta.i_mu.entity.EmailMessage;
//import com.sparta.i_mu.global.util.RedisUtil;
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Random;
//
//import static com.querydsl.core.alias.Alias.$;
//import static org.springframework.security.core.context.SecurityContextHolder.setContext;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class EmailService {
//
//    private final JavaMailSender javaMailSender;
//
//    private final UserService userService;
//    private  final RedisUtil redisUtil;
//
//
//    public String sendMail(EmailMessage emailMessage, String email, String type) {
//        String authNum = createCode();
//
//        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//
//        if (type.equals("password")) userService.SetTempPassword(emailMessage.getTo(), authNum);
//
//        try {
//            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
//            mimeMessageHelper.setTo(emailMessage.getTo()); //메일 수신자
//            mimeMessageHelper.setSubject(emailMessage.getSubject()); // 메일 제목
//            mimeMessageHelper.setText(authNum, true); // 메일 본문 내용, HTML 여부
//            javaMailSender.send(mimeMessage); // 메일 본문 내용
//
//            log.info("Success");
//
//            redisUtil.setDataExpire(email, authNum, 60 * 5L);
//
//            return null;
//
//        } catch (MessagingException e) {
//            log.info("fail");
//            throw new RuntimeException(e);
//        }
//    }
//
//
//
//    // 인증번호 및 임시 비밀번호 생성 메서드
//    public String createCode() {
//        Random random = new Random();
//        StringBuffer key = new StringBuffer();
//
//        for (int i = 0; i < 8; i++) {
//            int index = random.nextInt(4);
//
//            switch (index) {
//                case 0: key.append((char) ((int) random.nextInt(26) + 97)); break;
//                case 1: key.append((char) ((int) random.nextInt(26) + 65)); break;
//                default: key.append(random.nextInt(9));
//            }
//        }
//        return key.toString();
//    }
//
//
//    //인증코드 검증
//    public Boolean verifyEmailCode(String email, String code) {
//        String codeFoundByEmail = redisUtil.getData(email);
//        System.out.println(codeFoundByEmail);
//        if (codeFoundByEmail == null) {
//            return false;
//        }
//        redisUtil.removeData(email);
//        return codeFoundByEmail.equals(code);
//    }
//}
