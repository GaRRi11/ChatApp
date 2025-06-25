package com.gary.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.gary.web.dto.chatMessage.rest.ChatMessageResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, String> rateLimiterRedisTemplate(LettuceConnectionFactory factory) {
        return buildTemplate(String.class, factory);
    }

    @Bean
    public RedisTemplate<String, String> userPresenceRedisTemplate(LettuceConnectionFactory factory) {
        return buildTemplate(String.class, factory);
    }


    @Bean
    public RedisTemplate<String, ChatMessageResponse> chatMessageRedisTemplate(LettuceConnectionFactory factory) {
        return buildTemplate(ChatMessageResponse.class, factory);
    }

    private <T> RedisTemplate<String, T> buildTemplate(Class<T> clazz, LettuceConnectionFactory factory) {
        RedisTemplate<String, T> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // For String values, use plain StringRedisSerializer
        if (clazz.equals(String.class)) {
            StringRedisSerializer stringSerializer = new StringRedisSerializer();
            template.setKeySerializer(stringSerializer);
            template.setValueSerializer(stringSerializer);
            template.setHashKeySerializer(stringSerializer);
            template.setHashValueSerializer(stringSerializer);
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                    .allowIfSubType("com.gary")
                    .build();
            objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

            Jackson2JsonRedisSerializer<T> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, clazz);
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(serializer);
            template.setHashKeySerializer(new StringRedisSerializer());
            template.setHashValueSerializer(serializer);
        }

        template.afterPropertiesSet();
        return template;
    }
}




