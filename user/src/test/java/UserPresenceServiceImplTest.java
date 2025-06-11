
import com.gary.application.presence.UserPresenceServiceImpl;
import com.gary.infrastructure.constants.RedisKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserPresenceServiceImplTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UserPresenceServiceImpl userPresenceService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Manually inject @Value fields
        userPresenceService = new UserPresenceServiceImpl(redisTemplate);
        // Inject private fields
        java.lang.reflect.Field onlineStatusField = UserPresenceServiceImpl.class.getDeclaredField("onlineStatus");
        onlineStatusField.setAccessible(true);
        onlineStatusField.set(userPresenceService, "ONLINE");

        java.lang.reflect.Field expirationField = UserPresenceServiceImpl.class.getDeclaredField("expirationSeconds");
        expirationField.setAccessible(true);
        expirationField.set(userPresenceService, 300L);
    }

    @Test
    void refreshOnlineStatus_setsOnlineStatusInRedis() {
        Long userId = 1L;
        String key = RedisKeys.userPresence(userId);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        userPresenceService.refreshOnlineStatus(userId);

        verify(valueOperations).set(key, "ONLINE", 300L, TimeUnit.SECONDS);
    }

    @Test
    void setOffline_deletesPresenceKeyFromRedis() {
        Long userId = 2L;
        String key = RedisKeys.userPresence(userId);

        userPresenceService.setOffline(userId);

        verify(redisTemplate).delete(key);
    }

    @Test
    void isOnline_returnsTrueIfKeyExists() {
        Long userId = 3L;
        String key = RedisKeys.userPresence(userId);

        when(redisTemplate.hasKey(key)).thenReturn(true);

        boolean result = userPresenceService.isOnline(userId);

        assertThat(result).isTrue();
        verify(redisTemplate).hasKey(key);
    }

    @Test
    void isOnline_returnsFalseAndLogsIfRedisThrowsException() {
        Long userId = 4L;
        String key = RedisKeys.userPresence(userId);

        when(redisTemplate.hasKey(key)).thenThrow(new RuntimeException("Redis error"));

        boolean result = userPresenceService.isOnline(userId);

        assertThat(result).isFalse();
        verify(redisTemplate).hasKey(key);
    }
}
