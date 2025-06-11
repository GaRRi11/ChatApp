
import com.gary.application.rateLimiter.RateLimiterServiceImpl;
import com.gary.infrastructure.constants.RedisKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RateLimiterServiceImplTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private RateLimiterServiceImpl rateLimiterService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void isAllowedToSend_returnsTrue_whenUnderLimit() {
        Long userId = 1L;
        String key = RedisKeys.messageRateLimit(userId);
        Long redisResult = 3L;

        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                eq(Collections.singletonList(key)),
                eq("10")
        )).thenReturn(redisResult);

        boolean allowed = rateLimiterService.isAllowedToSend(userId);

        assertThat(allowed).isTrue();
    }

    @Test
    void isAllowedToSend_returnsFalse_whenLimitExceeded() {
        Long userId = 2L;
        String key = RedisKeys.messageRateLimit(userId);
        Long redisResult = 6L;

        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                eq(Collections.singletonList(key)),
                eq("10")
        )).thenReturn(redisResult);

        boolean allowed = rateLimiterService.isAllowedToSend(userId);

        assertThat(allowed).isFalse();
    }

    @Test
    void isAllowedToSend_returnsFalse_whenRedisReturnsNull() {
        Long userId = 3L;
        String key = RedisKeys.messageRateLimit(userId);

        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                eq(Collections.singletonList(key)),
                eq("10")
        )).thenReturn(null);

        boolean allowed = rateLimiterService.isAllowedToSend(userId);

        assertThat(allowed).isFalse(); // Defensive behavior
    }
}

