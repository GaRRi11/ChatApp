package com.gary.application.rateLimiter;

import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.common.time.TimeFormat;
import com.gary.domain.repository.cache.rateLimiter.RateLimiterCacheRepository;
import com.gary.domain.service.rateLimiter.RateLimiterService;
import com.gary.web.dto.cache.rateLimiter.RateLimiterCacheDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterServiceImpl implements RateLimiterService {

    private final RateLimiterCacheRepository  rateLimiterCacheRepository;

    @Value("${message.limit}")
    private int messageLimit;

    private Duration timeWindow;

    @Value("${time.window.seconds}")
    public void setTimeWindow(long seconds) {
        this.timeWindow = Duration.ofSeconds(seconds);
    }


    @Override
    @Timed("chat.rateLimiter.isAllowedToSend.duration")
    @Retry(name = "defaultRetry")
    @CircuitBreaker(name = "defaultCB", fallbackMethod = "rateLimiterFallback")
    @LoggableAction("Is Allowed To Send")
    public boolean isAllowedToSend(UUID userId) {


        RateLimiterCacheDto dto = rateLimiterCacheRepository.findById(userId).orElseGet(() ->
                RateLimiterCacheDto.builder()
                        .userId(userId)
                        .count(0)
                        .expiration(timeWindow.getSeconds())
                        .build()
        );

        dto.increment();
        rateLimiterCacheRepository.save(dto);

        if (dto.getCount() <= messageLimit) {
            return true;
        } else {
            log.warn("Timestamp='{}' User {} exceeded rate limit. Count: {}",
                    TimeFormat.nowTimestamp(), userId, dto.getCount());
            return false;
        }

    }

}
