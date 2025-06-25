package com.gary.web.dto.cache.presence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@RedisHash("user_presence")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPresenceCacheDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private UUID userId;

    private String status;

    @Builder.Default
    @TimeToLive(unit = TimeUnit.SECONDS)
    private long expiration = 60;

}
