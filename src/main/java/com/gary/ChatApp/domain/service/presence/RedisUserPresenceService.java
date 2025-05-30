package com.gary.ChatApp.domain.service.presence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisUserPresenceService implements UserPresenceService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final long ONLINE_EXPIRATION_SECONDS = 60;
    private static final String PREFIX = "user:presence:";

    private String key(Long userId) {
        return PREFIX + userId;
    }

    private static final String ONLINE_STATUS = "online";

    @Override
    public void refreshOnlineStatus(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        redisTemplate.opsForValue().set(key(userId), ONLINE_STATUS, ONLINE_EXPIRATION_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void setOffline(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        redisTemplate.delete(key(userId));
    }

    @Override
    public boolean isOnline(Long userId) {
        if (userId == null) return false;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key(userId)));
        } catch (Exception e) {
            log.error("Redis error when checking online status for userId {}", userId, e);
            return false;
        }
    }

}
