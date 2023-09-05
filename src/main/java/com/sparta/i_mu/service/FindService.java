package com.sparta.i_mu.service;

import com.sparta.i_mu.entity.User;
import com.sparta.i_mu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class FindService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void changePassword(String email, String newPassword) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (!optionalUser.isPresent()) {
            throw new UserNotFoundException("User with email " + email + " not found");
        }

        User user = optionalUser.get();
        // 새로운 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        userRepository.save(user);
    }


    public String findemail(String to){
        Optional<User> userinfo = userRepository.findByPhonenumber(to);
        User user = userinfo.get();
        String useremail = user.getEmail();
        return useremail;
    }

    public class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}
