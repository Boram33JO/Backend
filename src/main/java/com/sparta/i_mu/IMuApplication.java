package com.sparta.i_mu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication(exclude = SecurityAutoConfiguration.class) //Spring Security 인증 기능 제어
public class IMuApplication {

    public static void main(String[] args) {
        SpringApplication.run(IMuApplication.class, args);
    }

}
