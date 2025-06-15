package com.gary.domain.service.rateLimiter;


import com.gary.application.cache.rateLimiter.RateLimiterServiceImpl;
import com.gary.application.cache.rateLimiter.RateLimiterStatus;

import java.util.UUID;

public interface RateLimiterService {

    RateLimiterStatus isAllowedToSend(UUID userId);
    }
