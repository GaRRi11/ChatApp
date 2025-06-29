package com.gary;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableRetry
@EnableJpaRepositories(basePackages = "com.gary.domain.repository.jpa")
@EnableRedisRepositories(basePackages = "com.gary.domain.repository.cache")
@EnableRedisDocumentRepositories(basePackages = "com.gary.web.dto.cache")
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);

    }
}