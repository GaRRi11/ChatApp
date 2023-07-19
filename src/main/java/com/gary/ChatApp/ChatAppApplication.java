package com.gary.ChatApp;

import com.gary.ChatApp.storage.repository.redis.RedisChatMessageRepository;
import com.gary.ChatApp.web.config.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.gary.ChatApp.storage.repository.jpa")
@EnableRedisRepositories(basePackages = "com.gary.ChatApp.storage.repository.redis",repositoryBaseClass = RedisChatMessageRepository.class)
public class ChatAppApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = 	SpringApplication.run(ChatAppApplication.class, args);
		AppConfig beanInspector = context.getBean(AppConfig.class);
		beanInspector.printBeanNames();
	}

}
