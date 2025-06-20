package com.gary.application.presence;

import com.gary.annotations.LoggableAction;
import com.gary.annotations.Timed;
import com.gary.application.common.MetricIncrement;
import com.gary.application.common.TimeFormat;
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
    private final MetricIncrement metricIncrement;

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
        metricIncrement.incrementMetric("presence.set.online", "success");
    }

    void refreshOnlineStatusFallback(UUID userId, Throwable t) {
        log.warn("Timestamp='{}' Failed to refresh online status for user {}. Cause: {}",
                TimeFormat.nowTimestamp(),
                userId,
                t.toString());
        metricIncrement.incrementMetric("presence.set.online", "fallback");
    }


    @Override
    @LoggableAction("Set Offline Status")
    @Timed("presence.setOffline.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "setOfflineFallback")
    public void setOffline(UUID userId) {
        userPresenceRedisTemplate.delete(RedisKeys.userPresence(userId));
        metricIncrement.incrementMetric("presence.set.offline", "success");
    }

    void setOfflineFallback(UUID userId, Throwable t) {
        log.warn("Timestamp='{}' Failed to set user {} offline. Cause: {}",
                TimeFormat.nowTimestamp(),
                userId,
                t.toString());
        metricIncrement.incrementMetric("presence.set.offline", "fallback");
    }


    @Override
    @LoggableAction("Check Online Status")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "isOnlineFallback")
    public boolean isOnline(UUID userId) {
        return userPresenceRedisTemplate.hasKey(RedisKeys.userPresence(userId));
    }

    boolean isOnlineFallback(UUID userId, Throwable t) {
        log.warn("Timestamp='{}' Failed to check online status for user {}. Cause: {}",
                TimeFormat.nowTimestamp(),
                userId,
                t.toString());
        return false;
    }
}
