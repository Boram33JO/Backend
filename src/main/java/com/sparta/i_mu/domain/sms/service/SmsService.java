package com.sparta.i_mu.domain.sms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.i_mu.domain.sms.dto.SmsInfo;
import com.sparta.i_mu.domain.sms.dto.SmsMessageRequestDto;
import com.sparta.i_mu.domain.sms.dto.SmsResponseDto;
import com.sparta.i_mu.domain.user.entity.User;
import com.sparta.i_mu.domain.user.repository.UserRepository;
import com.sparta.i_mu.global.exception.UserNotFoundException;
import com.sparta.i_mu.global.responseResource.ResponseResource;
import com.sparta.i_mu.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.utils.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

// sms
@Slf4j
@RequiredArgsConstructor
@Service
public class SmsService {
    private final RedisUtil redisUtil;
    private final UserRepository userRepository;

    @Value("${naver-cloud-sms.accessKey}")
    private String accessKey;

    @Value("${naver-cloud-sms.secretKey}")
    private String secretKey;

    @Value("${naver-cloud-sms.serviceId}")
    private String serviceId;

    @Value("${naver-cloud-sms.senderPhone}")
    private String phone;

    public String getSignature(String time) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = "/sms/v2/services/" + this.serviceId + "/messages";
        String accessKey = this.accessKey;
        String secretKey = this.secretKey;

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(time)
                .append(newLine)
                .append(accessKey)
                .toString();

        SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
        String encodeBase64String = Base64.encodeBase64String(rawHmac);

        return encodeBase64String;
    }

    public SmsResponseDto sendSign(SmsMessageRequestDto smsMessageRequestDto) throws JsonProcessingException, RestClientException, URISyntaxException, InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {

        Optional<User> optionalUser = userRepository.findByPhoneNumber(smsMessageRequestDto.getTo());

        if (optionalUser.isPresent()) {
            throw new UserNotFoundException("User with phoneNumber " + smsMessageRequestDto + " Already exist");
        }

        String time = Long.toString(System.currentTimeMillis());
        String smsConfirmNum = createSmsKey();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-ncp-apigw-timestamp", time);
        headers.set("x-ncp-iam-access-key", accessKey);
        headers.set("x-ncp-apigw-signature-v2", getSignature(time)); // signature 서명

        List<SmsMessageRequestDto> messages = new ArrayList<>();
        messages.add(smsMessageRequestDto);

        SmsInfo request = SmsInfo.builder()
                .type("SMS")
                .contentType("COMM")
                .countryCode("82")
                .from(phone)
                .content("[P.PLE] 인증번호 [" + smsConfirmNum + "]를 입력해주세요")
                .messages(messages)
                .build();

        //쌓은 바디를 json형태로 반환
        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(request);
        // jsonBody와 헤더 조립
        HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        //restTemplate로 post 요청 보내고 오류가 없으면 202코드 반환
        SmsResponseDto smsResponseDto = restTemplate.postForObject(new URI("https://sens.apigw.ntruss.com/sms/v2/services/" + serviceId + "/messages"), httpBody, SmsResponseDto.class);

        String phoneNumber = smsMessageRequestDto.getTo();
        redisUtil.setAuthNumData(phoneNumber, smsConfirmNum, 60 * 5L);

        return smsResponseDto;

    }

    public SmsResponseDto sendPw(SmsMessageRequestDto smsMessageRequestDto) throws JsonProcessingException, RestClientException, URISyntaxException, InvalidKeyException, NoSuchAlgorithmException, UnsupportedEncodingException {

        Optional<User> optionalUser = userRepository.findByPhoneNumber(smsMessageRequestDto.getTo());

        if (!optionalUser.isPresent()) {
            throw new UserNotFoundException("User with phoneNumber " + smsMessageRequestDto + " Already exist");
        }

        String time = Long.toString(System.currentTimeMillis());
        String smsConfirmNum = createSmsKey();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-ncp-apigw-timestamp", time);
        headers.set("x-ncp-iam-access-key", accessKey);
        headers.set("x-ncp-apigw-signature-v2", getSignature(time)); // signature 서명

        List<SmsMessageRequestDto> messages = new ArrayList<>();
        messages.add(smsMessageRequestDto);

        SmsInfo request = SmsInfo.builder()
                .type("SMS")
                .contentType("COMM")
                .countryCode("82")
                .from(phone)
                .content("[P.PLE] 인증번호 [" + smsConfirmNum + "]를 입력해주세요")
                .messages(messages)
                .build();

        //쌓은 바디를 json형태로 반환
        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(request);
        // jsonBody와 헤더 조립
        HttpEntity<String> httpBody = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        //restTemplate로 post 요청 보내고 오류가 없으면 202코드 반환
        SmsResponseDto smsResponseDto = restTemplate.postForObject(new URI("https://sens.apigw.ntruss.com/sms/v2/services/" + serviceId + "/messages"), httpBody, SmsResponseDto.class);

        String phoneNumber = smsMessageRequestDto.getTo();
        redisUtil.setAuthNumData(phoneNumber, smsConfirmNum, 60 * 5L);

        return smsResponseDto;

    }


    // 인증코드 만들기
    public static String createSmsKey() {
        StringBuffer key = new StringBuffer();
        Random rnd = new Random();

        for (int i = 0; i < 5; i++) { // 인증코드 5자리
            key.append((rnd.nextInt(10)));
        }
        return key.toString();
    }

    public Boolean verifyPhoneCode_signUp(String phoneNumber, String confirmNum) {
        String storedCode = redisUtil.getAuthNumData(phoneNumber);

        if (storedCode != null && storedCode.equals(confirmNum)) {
            redisUtil.removeAuthNumData(phoneNumber);  // 코드가 일치하면 코드를 제거합니다.
            return true;
        }
        return false;  // 코드가 일치하지 않거나 데이터가 없으면 false
    }

    public ResponseResource<?> verifyPhoneCode_findEmail(String phoneNumber, String confirmNum) {

        String storedCode = redisUtil.getAuthNumData(phoneNumber);
        if (storedCode != null && storedCode.equals(confirmNum)) {
            redisUtil.removeAuthNumData(phoneNumber);  // 코드가 일치하면 코드를 제거합니다.

            User finduser = userRepository.findByPhoneNumber(phoneNumber).orElseThrow(() ->
                    new IllegalArgumentException("찾으시는 유저가 존재하지 않습니다."));

            String findUserEmail = finduser.getEmail();

            return ResponseResource.data(findUserEmail, HttpStatus.OK, "휴대폰 번호 인증에 성공하셨습니다.");

        } else
            return ResponseResource.error("휴대폰 인증에 실패하였습니다.", HttpStatus.BAD_REQUEST.value());
    }
}

