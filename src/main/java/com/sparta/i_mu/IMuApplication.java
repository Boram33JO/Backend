package com.sparta.i_mu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class IMuApplication {

    public static void main(String[] args) {
        SpringApplication.run(IMuApplication.class, args);
    }

}
