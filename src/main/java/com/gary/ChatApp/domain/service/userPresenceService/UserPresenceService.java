package com.gary.ChatApp.domain.service.userPresenceService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserPresenceService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ONLINE_KEY_PREFIX = "user:online:";
    private static final Duration ONLINE_TTL = Duration.ofMinutes(5);

    public void setOnline(Long userId) {
        redisTemplate.opsForValue().set(ONLINE_KEY_PREFIX + userId, true, ONLINE_TTL);
    }

    public void setOffline(Long userId) {
        redisTemplate.delete(ONLINE_KEY_PREFIX + userId);
    }

    public boolean isOnline(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(ONLINE_KEY_PREFIX + userId));
    }

    public void refreshOnlineStatus(Long userId) {
        String key = ONLINE_KEY_PREFIX + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.expire(key, ONLINE_TTL);
        } else {
            setOnline(userId);
        }
    }
}
