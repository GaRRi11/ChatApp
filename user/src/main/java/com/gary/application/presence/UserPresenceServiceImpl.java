package com.gary.application.presence;

import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.common.metric.MetricIncrement;
import com.gary.common.time.TimeFormat;
import com.gary.domain.repository.cache.presence.UserPresenceCacheRepository;
import com.gary.domain.service.presence.UserPresenceService;
import com.gary.web.dto.cache.presence.UserPresenceCacheDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserPresenceServiceImpl implements UserPresenceService {

    private final MetricIncrement metricIncrement;
    private final UserPresenceCacheRepository userPresenceCacheRepository;

    @Value("${presence.status.online}")
    private String onlineStatus;


    @Override
    @LoggableAction("Refresh Online Status")
    @Timed("presence.RefreshOnline.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "refreshOnlineStatusFallback")
    public void refreshOnlineStatus(UUID userId) {

        UserPresenceCacheDto userPresenceCacheDto = UserPresenceCacheDto.builder()
                .userId(userId)
                .status(onlineStatus)
                .build();

        userPresenceCacheRepository.save(userPresenceCacheDto);

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

        userPresenceCacheRepository.deleteById(userId);

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
        return userPresenceCacheRepository.existsById(userId);
    }

    boolean isOnlineFallback(UUID userId, Throwable t) {
        log.warn("Timestamp='{}' Failed to check online status for user {}. Cause: {}",
                TimeFormat.nowTimestamp(),
                userId,
                t.toString());
        return false;
    }
}
