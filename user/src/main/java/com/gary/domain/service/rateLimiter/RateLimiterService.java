package com.gary.domain.service.rateLimiter;


import java.util.UUID;

public interface RateLimiterService {

    boolean isAllowedToSend(UUID userId);

}
