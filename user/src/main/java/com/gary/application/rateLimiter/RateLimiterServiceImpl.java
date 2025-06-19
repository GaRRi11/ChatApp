package com.gary.application.rateLimiter;

import com.gary.annotations.LoggableAction;
import com.gary.annotations.Timed;
import com.gary.application.common.MetricIncrement;
import com.gary.infrastructure.constants.RedisKeys;
import com.gary.domain.service.rateLimiter.RateLimiterService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterServiceImpl implements RateLimiterService {

    private final RedisTemplate<String, String> rateLimiterRedisTemplate;

    private final MetricIncrement metricIncrement;

    @Value("${message.limit}")
    private int messageLimit;

    private Duration timeWindow;

    @Value("${time.window.seconds}")
    public void setTimeWindow(long seconds) {
        this.timeWindow = Duration.ofSeconds(seconds);
    }


    private static final String LUA_SCRIPT =
            "local current\n" +
                    "current = redis.call('incr', KEYS[1])\n" +
                    "if tonumber(current) == 1 then\n" +
                    "  redis.call('expire', KEYS[1], ARGV[1])\n" +
                    "end\n" +
                    "return current";


    @Override
    @Timed("chat.rateLimiter.isAllowedToSend.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "rateLimiterFallback")
    @LoggableAction("Is Allowed To Send")
    public RateLimiterStatus isAllowedToSend(UUID userId) {

        String key = RedisKeys.messageRateLimit(userId);

        Long current = rateLimiterRedisTemplate.execute(
                new DefaultRedisScript<>(LUA_SCRIPT, Long.class),
                Collections.singletonList(key),
                String.valueOf(timeWindow.getSeconds())
        );

        if (current == null) {
            log.warn("Redis returned null for rate limit key {}", key);
            metricIncrement.incrementMetric("rateLimiter.isAllowed","fallback");
            return RateLimiterStatus.UNAVAILABLE;
        }

        boolean allowed = current <= messageLimit;

        if (allowed) {
            metricIncrement.incrementMetric("rateLimiter.isAllowed","allowed");
            return RateLimiterStatus.ALLOWED;
        } else {
            metricIncrement.incrementMetric("rateLimiter.isAllowed","rejected");
            return RateLimiterStatus.BLOCKED;
        }
    }

    @LoggableAction("Rate Limiter Fallback")
    RateLimiterStatus rateLimiterFallback(UUID userId, Throwable t) {
        log.error("Fallback triggered for userId {} due to {}", userId, t.toString());
        metricIncrement.incrementMetric("rateLimiter.isAllowed","fallback");
        return RateLimiterStatus.UNAVAILABLE;
    }
}
