package com.gary.domain.service.rateLimiter;


public interface RateLimiterService {

     boolean isAllowedToSend(Long userId);
    }
