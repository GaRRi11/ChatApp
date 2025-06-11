
import com.gary.application.token.RefreshTokenServiceImpl;
import com.gary.domain.model.token.RefreshToken;
import com.gary.infrastructure.constants.RedisKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.SetOperations;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RefreshTokenServiceImplTest {

    @Mock
    private RedisTemplate<String, RefreshToken> refreshTokenRedisTemplate;

    @Mock
    private RedisTemplate<String, String> stringRedisTemplate;

    @Mock
    private ValueOperations<String, RefreshToken> refreshTokenOps;

    @Mock
    private ValueOperations<String, String> stringValueOps;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private static final long TTL = 7 * 24 * 60 * 60;
    private final Long userId = 1L;
    private final String token = "test-token";

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(refreshTokenRedisTemplate.opsForValue()).thenReturn(refreshTokenOps);
        when(stringRedisTemplate.opsForValue()).thenReturn(stringValueOps);
        when(stringRedisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    void save_shouldStoreRefreshTokenAndAddToUserSet() {
        refreshTokenService.save(userId, token);

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenOps).set(eq(RedisKeys.refreshToken(token)), tokenCaptor.capture(), eq(java.time.Duration.ofSeconds(TTL)));
        verify(setOperations).add(RedisKeys.refreshTokenSet(userId), token);
        verify(stringRedisTemplate).expire(RedisKeys.refreshTokenSet(userId), java.time.Duration.ofSeconds(TTL));

        RefreshToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getToken()).isEqualTo(token);
        assertThat(savedToken.getUserId()).isEqualTo(userId);
        assertThat(savedToken.isRevoked()).isFalse();
    }

    @Test
    void isValid_shouldReturnTrue_whenTokenIsValid() {
        RefreshToken tokenObj = RefreshToken.builder()
                .token(token)
                .userId(userId)
                .revoked(false)
                .expiryDate(Instant.now().plusSeconds(1000).toEpochMilli())
                .build();

        when(refreshTokenOps.get(RedisKeys.refreshToken(token))).thenReturn(tokenObj);

        boolean result = refreshTokenService.isValid(token);

        assertThat(result).isTrue();
    }

    @Test
    void isValid_shouldReturnFalse_whenTokenIsExpired() {
        RefreshToken tokenObj = RefreshToken.builder()
                .token(token)
                .userId(userId)
                .revoked(false)
                .expiryDate(Instant.now().minusSeconds(10).toEpochMilli())
                .build();

        when(refreshTokenOps.get(RedisKeys.refreshToken(token))).thenReturn(tokenObj);

        boolean result = refreshTokenService.isValid(token);

        assertThat(result).isFalse();
        verify(refreshTokenRedisTemplate).delete(RedisKeys.refreshToken(token));
    }

    @Test
    void isValid_shouldReturnFalse_whenTokenIsRevoked() {
        RefreshToken tokenObj = RefreshToken.builder()
                .token(token)
                .userId(userId)
                .revoked(true)
                .expiryDate(Instant.now().plusSeconds(1000).toEpochMilli())
                .build();

        when(refreshTokenOps.get(RedisKeys.refreshToken(token))).thenReturn(tokenObj);

        boolean result = refreshTokenService.isValid(token);

        assertThat(result).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenTokenNotFound() {
        when(refreshTokenOps.get(RedisKeys.refreshToken(token))).thenReturn(null);

        boolean result = refreshTokenService.isValid(token);

        assertThat(result).isFalse();
    }

    @Test
    void revoke_shouldUpdateTokenAsRevoked_whenTokenExists() {
        RefreshToken tokenObj = RefreshToken.builder()
                .token(token)
                .userId(userId)
                .revoked(false)
                .expiryDate(Instant.now().plusSeconds(1000).toEpochMilli())
                .build();

        when(refreshTokenOps.get(RedisKeys.refreshToken(token))).thenReturn(tokenObj);
        when(refreshTokenRedisTemplate.getExpire(RedisKeys.refreshToken(token), TimeUnit.SECONDS)).thenReturn(3600L);

        refreshTokenService.revoke(token);

        assertThat(tokenObj.isRevoked()).isTrue();
        verify(refreshTokenOps).set(eq(RedisKeys.refreshToken(token)), eq(tokenObj), eq(java.time.Duration.ofSeconds(3600)));
    }

    @Test
    void revoke_shouldLogWarning_whenTokenDoesNotExist() {
        when(refreshTokenOps.get(RedisKeys.refreshToken(token))).thenReturn(null);
        refreshTokenService.revoke(token);
        verify(refreshTokenOps, never()).set(any(), any(), any());
    }

    @Test
    void revokeAll_shouldRevokeAllUserTokens() {
        String token1 = "t1";
        String token2 = "t2";
        Set<String> tokens = Set.of(token1, token2);

        RefreshToken t1 = RefreshToken.builder()
                .token(token1)
                .userId(userId)
                .revoked(false)
                .expiryDate(Instant.now().plusSeconds(1000).toEpochMilli())
                .build();
        RefreshToken t2 = RefreshToken.builder()
                .token(token2)
                .userId(userId)
                .revoked(false)
                .expiryDate(Instant.now().plusSeconds(1000).toEpochMilli())
                .build();

        when(setOperations.members(RedisKeys.refreshTokenSet(userId))).thenReturn(tokens);
        when(refreshTokenOps.get(RedisKeys.refreshToken(token1))).thenReturn(t1);
        when(refreshTokenOps.get(RedisKeys.refreshToken(token2))).thenReturn(t2);
        when(refreshTokenRedisTemplate.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenReturn(3600L);

        refreshTokenService.revokeAll(userId);

        assertThat(t1.isRevoked()).isTrue();
        assertThat(t2.isRevoked()).isTrue();
        verify(refreshTokenOps, times(2)).set(anyString(), any(), eq(java.time.Duration.ofSeconds(3600)));
    }
}
