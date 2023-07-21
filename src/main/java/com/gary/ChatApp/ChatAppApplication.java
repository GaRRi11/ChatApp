package com.gary.ChatApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableJpaRepositories(basePackages = "com.gary.ChatApp.storage.repository.jpa")
//@EnableRedisRepositories(basePackages = "com.gary.ChatApp.storage.repository.redis",repositoryBaseClass = RedisChatMessageRepository.class)
public class ChatAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatAppApplication.class, args);
	}

}
