import com.gary.application.chat.ChatMessageServiceImpl;
import com.gary.domain.model.chatmessage.ChatMessage;
import com.gary.domain.repository.chatMessage.ChatMessageRepository;
import com.gary.domain.service.cache.ChatCacheService;
import com.gary.domain.service.rateLimiter.RateLimiterService;
import com.gary.web.dto.chatMessage.ChatMessageRequest;
import com.gary.web.dto.chatMessage.ChatMessageResponse;
import com.gary.web.exception.TooManyRequestsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatMessageServiceImplTest {

    private ChatMessageRepository chatMessageRepository;
    private RateLimiterService rateLimiterService;
    private ChatCacheService chatCacheService;
    private ChatMessageServiceImpl chatMessageService;

    @BeforeEach
    void setUp() {
        chatMessageRepository = mock(ChatMessageRepository.class);
        rateLimiterService = mock(RateLimiterService.class);
        chatCacheService = mock(ChatCacheService.class);
        chatMessageService = new ChatMessageServiceImpl(chatMessageRepository, rateLimiterService, chatCacheService);
    }

    @Test
    void sendMessage_shouldSaveMessageAndCacheResponse_whenAllowed() {
        Long senderId = 1L;
        ChatMessageRequest request = new ChatMessageRequest(2L, "  Hello!  ");

        ChatMessage savedMessage = ChatMessage.builder()
                .id(1L)
                .senderId(senderId)
                .receiverId(request.receiverId())
                .content("Hello!")
                .timestamp(LocalDateTime.now())
                .build();

        when(rateLimiterService.isAllowedToSend(senderId)).thenReturn(true);
        when(chatMessageRepository.save(any())).thenReturn(savedMessage);

        ChatMessageResponse expectedResponse = ChatMessageResponse.fromEntity(savedMessage);

        ChatMessageResponse actualResponse = chatMessageService.sendMessage(request, senderId);

        assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse);
        verify(chatMessageRepository).save(any(ChatMessage.class));
        verify(chatCacheService).cacheMessage(eq(expectedResponse));
    }

    @Test
    void sendMessage_shouldThrowTooManyRequests_whenRateLimited() {
        Long senderId = 1L;
        ChatMessageRequest request = new ChatMessageRequest(2L, "Hey");

        when(rateLimiterService.isAllowedToSend(senderId)).thenReturn(false);

        assertThatThrownBy(() -> chatMessageService.sendMessage(request, senderId))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessageContaining("sending messages too quickly");

        verifyNoInteractions(chatMessageRepository, chatCacheService);
    }

    @Test
    void getChatHistory_shouldReturnCachedMessages_whenCacheHit() {
        Long user1Id = 1L, user2Id = 2L;
        int offset = 0, limit = 10;

        List<ChatMessageResponse> cachedMessages = List.of(
                new ChatMessageResponse(1L, user1Id, user2Id, "Hi", LocalDateTime.now())
        );

        when(chatCacheService.getCachedMessages(user1Id, user2Id, offset, limit)).thenReturn(cachedMessages);

        List<ChatMessageResponse> result = chatMessageService.getChatHistory(user1Id, user2Id, offset, limit);

        assertThat(result).isEqualTo(cachedMessages);
        verifyNoInteractions(chatMessageRepository);
    }

    @Test
    void getChatHistory_shouldQueryRepositoryAndCache_whenCacheMiss() {
        Long user1Id = 1L, user2Id = 2L;
        int offset = 0, limit = 10;

        when(chatCacheService.getCachedMessages(user1Id, user2Id, offset, limit)).thenReturn(List.of());

        ChatMessage dbMessage = ChatMessage.builder()
                .id(1L)
                .senderId(user1Id)
                .receiverId(user2Id)
                .content("Hi from DB")
                .timestamp(LocalDateTime.now())
                .build();

        when(chatMessageRepository.findChatBetweenUsers(user1Id, user2Id, offset, limit))
                .thenReturn(List.of(dbMessage));

        List<ChatMessageResponse> result = chatMessageService.getChatHistory(user1Id, user2Id, offset, limit);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).content()).isEqualTo("Hi from DB");

        verify(chatMessageRepository).findChatBetweenUsers(user1Id, user2Id, offset, limit);
        verify(chatCacheService).cacheMessage(any(ChatMessageResponse.class));
    }
}
