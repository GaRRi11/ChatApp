package com.gary.domain.service.rateLimiter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String MESSAGE_LIMIT_PREFIX = "rate:message:";
    private static final int MESSAGE_LIMIT = 5;
    private static final Duration TIME_WINDOW = Duration.ofSeconds(10);

    public boolean isAllowedToSend(Long userId) {
        String key = MESSAGE_LIMIT_PREFIX + userId;

        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, TIME_WINDOW);
        }

        return count <= MESSAGE_LIMIT;
    }
}
