package com.gary.ChatApp.domain.service.userPresenceService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisUserPresenceService implements UserPresenceService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final long ONLINE_EXPIRATION_SECONDS = 60;
    private static final String PREFIX = "user:presence:";

    private String key(Long userId) {
        return PREFIX + userId;
    }

    @Override
    public void refreshOnlineStatus(Long userId) {
        redisTemplate.opsForValue().set(key(userId), "online", ONLINE_EXPIRATION_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void setOffline(Long userId) {
        redisTemplate.delete(key(userId));
    }

    @Override
    public boolean isOnline(Long userId) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key(userId)));
        } catch (Exception e) {
            // Optional: log the error or handle it gracefully
            return false;
        }
    }
}
