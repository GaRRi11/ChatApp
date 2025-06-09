package com.gary.domain.service.rateLimiter;

import com.gary.config.RedisConfig;
import com.gary.config.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;



import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String, String> rateLimiterRedisTemplate;

    private static final int MESSAGE_LIMIT = 5;
    private static final Duration TIME_WINDOW = Duration.ofSeconds(10);

    private static final String LUA_SCRIPT =
            "local current\n" +
                    "current = redis.call('incr', KEYS[1])\n" +
                    "if tonumber(current) == 1 then\n" +
                    "  redis.call('expire', KEYS[1], ARGV[1])\n" +
                    "end\n" +
                    "return current";

    public boolean isAllowedToSend(Long userId) {
        String key = RedisKeys.messageRateLimit(userId);

        Long current = rateLimiterRedisTemplate.execute(
                new DefaultRedisScript<>(LUA_SCRIPT, Long.class),
                Collections.singletonList(key),
                String.valueOf(TIME_WINDOW.getSeconds())
        );

        return current != null && current <= MESSAGE_LIMIT;
    }
}
