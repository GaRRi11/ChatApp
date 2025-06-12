import com.gary.application.cache.chat.ChatCacheServiceImpl;
import com.gary.infrastructure.constants.RedisKeys;
import com.gary.web.dto.chatMessage.ChatMessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ListOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ChatCacheServiceImplTest {

    @Mock
    private RedisTemplate<String, ChatMessageResponse> redisTemplate;

    @Mock
    private ListOperations<String, ChatMessageResponse> listOperations;

    @InjectMocks
    private ChatCacheServiceImpl chatCacheService;

    @Captor
    private ArgumentCaptor<String> keyCaptor;

    private final Long senderId = 1L;
    private final Long receiverId = 2L;
    private final String expectedKey = RedisKeys.chatMessages(senderId, receiverId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    void cacheMessage_shouldPushToRedisAndSetExpirationIfNewKey() {
        // given
        ChatMessageResponse message = new ChatMessageResponse(1L, senderId, receiverId, "hi", LocalDateTime.now());
        when(redisTemplate.hasKey(expectedKey)).thenReturn(false);

        // when
        chatCacheService.cacheMessage(message);

        // then
        verify(listOperations).rightPush(expectedKey, message);
        verify(redisTemplate).expire(expectedKey, Duration.ofHours(6));
    }

    @Test
    void cacheMessage_shouldNotSetExpirationIfKeyExists() {
        // given
        ChatMessageResponse message = new ChatMessageResponse(1L, senderId, receiverId, "hi", LocalDateTime.now());
        when(redisTemplate.hasKey(expectedKey)).thenReturn(true);

        // when
        chatCacheService.cacheMessage(message);

        // then
        verify(listOperations).rightPush(expectedKey, message);
        verify(redisTemplate, never()).expire(any(), any());
    }

    @Test
    void getCachedMessages_shouldReturnRangeFromRedis() {
        // given
        int offset = 0;
        int limit = 2;
        List<ChatMessageResponse> mockMessages = List.of(
                new ChatMessageResponse(1L, senderId, receiverId, "hi", LocalDateTime.now()),
                new ChatMessageResponse(2L, senderId, receiverId, "hello", LocalDateTime.now())
        );

        when(listOperations.range(expectedKey, offset, offset + limit - 1)).thenReturn(mockMessages);

        // when
        List<ChatMessageResponse> result = chatCacheService.getCachedMessages(senderId, receiverId, offset, limit);

        // then
        assertThat(result).hasSize(2).containsExactlyElementsOf(mockMessages);
    }

    @Test
    void evictChatCache_shouldDeleteRedisKey() {
        // when
        chatCacheService.clearCachedMessages(senderId, receiverId);

        // then
        verify(redisTemplate).delete(expectedKey);
    }
}
