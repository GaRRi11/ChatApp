package com.gary.web.dto.cache.rateLimiter;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("rateLimiter")
public class RateLimiterCacheDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private UUID userId;

    private int count;

    @Builder.Default
    @TimeToLive(unit = TimeUnit.SECONDS)
    private long expiration = 60;

    public void increment() {
        this.count++;
    }
}

