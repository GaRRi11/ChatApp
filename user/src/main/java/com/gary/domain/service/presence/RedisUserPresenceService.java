package com.gary.domain.service.presence;

import com.gary.config.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisUserPresenceService implements UserPresenceService {

    private final RedisTemplate<String, String> userPresenceRedisTemplate;
    private static final long ONLINE_EXPIRATION_SECONDS = 60;
    private static final String ONLINE_STATUS = "online";

    @Override
    public void refreshOnlineStatus(Long userId) {
        userPresenceRedisTemplate.opsForValue().set(
                RedisKeys.userPresence(userId),
                ONLINE_STATUS,
                ONLINE_EXPIRATION_SECONDS,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void setOffline(Long userId) {
        userPresenceRedisTemplate.delete(RedisKeys.userPresence(userId));
    }

    @Override
    public boolean isOnline(Long userId) {
        try {
            return Boolean.TRUE.equals(userPresenceRedisTemplate.hasKey(RedisKeys.userPresence(userId)));
        } catch (Exception e) {
            log.error("Redis error while checking online status for userId {}", userId, e);
            return false;
        }
    }
}
