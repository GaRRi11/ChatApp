package com.gary.domain.repository.cache.rateLimiter;

import com.gary.web.dto.cache.rateLimiter.RateLimiterCacheDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RateLimiterCacheRepository extends CrudRepository<RateLimiterCacheDto, UUID> {
}
