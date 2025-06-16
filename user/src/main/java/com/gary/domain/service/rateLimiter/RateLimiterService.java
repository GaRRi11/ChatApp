package com.gary.domain.service.rateLimiter;


import com.gary.application.rateLimiter.RateLimiterStatus;

import java.util.UUID;

public interface RateLimiterService {

    RateLimiterStatus isAllowedToSend(UUID userId);

    RateLimiterStatus rateLimiterFallback(UUID userId, Throwable t);
    }
