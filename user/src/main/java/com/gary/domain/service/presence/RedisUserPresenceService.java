package com.gary.domain.service.presence;

import com.gary.config.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class RedisUserPresenceService implements UserPresenceService {

    private final RedisTemplate<String, String> userPresenceRedisTemplate;

    @Value("${presence.status.online}")
    private String onlineStatus;

    @Value("${presence.expiration.seconds}")
    private long expirationSeconds;


    @Override
    public void refreshOnlineStatus(Long userId) {
        userPresenceRedisTemplate.opsForValue().set(
                RedisKeys.userPresence(userId),
                onlineStatus,
                expirationSeconds,
                TimeUnit.SECONDS
        );
        log.debug("Refreshed online status for user {}", userId);
    }

    @Override
    public void setOffline(Long userId) {
        userPresenceRedisTemplate.delete(RedisKeys.userPresence(userId));
        log.debug("User {}, set offline", userId);
    }

    @Override
    public boolean isOnline(Long userId) {
        try {
            return Boolean.TRUE.equals(userPresenceRedisTemplate.hasKey(RedisKeys.userPresence(userId)));
        } catch (Exception e) {
            log.error("Redis error while checking online status for userId {}", userId, e);
            return false;
        }
    }
}
