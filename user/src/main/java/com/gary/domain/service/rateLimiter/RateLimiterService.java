package com.gary.domain.service.rateLimiter;

import org.springframework.stereotype.Service;

public interface RateLimiterService {

     boolean isAllowedToSend(Long userId);
    }
