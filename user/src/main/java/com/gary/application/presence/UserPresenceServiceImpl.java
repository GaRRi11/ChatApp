package com.gary.application.presence;

import com.gary.annotations.LoggableAction;
import com.gary.annotations.Timed;
import com.gary.infrastructure.constants.RedisKeys;
import com.gary.domain.service.presence.UserPresenceService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserPresenceServiceImpl implements UserPresenceService {

    private final RedisTemplate<String, String> userPresenceRedisTemplate;

    @Value("${presence.status.online}")
    private String onlineStatus;

    @Value("${presence.expiration.seconds}")
    private long expirationSeconds;


    @Override
    @LoggableAction("Refresh Online Status")
    @Timed("presence.RefreshOnline.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "getCachedMessagesFallback")
    public void refreshOnlineStatus(UUID userId) {
        userPresenceRedisTemplate.opsForValue().set(
                RedisKeys.userPresence(userId),
                onlineStatus,
                expirationSeconds,
                TimeUnit.SECONDS
        );
        log.debug("Refreshed online status for user {}", userId);
    }

    @Override
    public void setOffline(UUID userId) {
        userPresenceRedisTemplate.delete(RedisKeys.userPresence(userId));
        log.debug("User {}, set offline", userId);
    }

    @Override
    public boolean isOnline(UUID userId) {
        try {
            return userPresenceRedisTemplate.hasKey(RedisKeys.userPresence(userId));
        } catch (Exception e) {
            log.error("Redis error while checking online status for userId {}", userId, e);
            return false;
        }
    }
}
