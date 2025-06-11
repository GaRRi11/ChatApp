
import com.gary.application.friendship.FriendshipManager;
import com.gary.application.friendship.FriendshipServiceImpl;
import com.gary.domain.event.friendshipRemoved.FriendshipRemovedEvent;
import com.gary.domain.model.friendship.Friendship;
import com.gary.domain.model.user.User;
import com.gary.domain.repository.friendship.FriendshipRepository;
import com.gary.domain.service.user.UserService;
import com.gary.web.dto.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FriendshipServiceImplTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserService userService;

    @Mock
    private FriendshipManager friendshipManager;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FriendshipServiceImpl friendshipService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getFriends_returnsListOfUserResponses() {

        Long userId = 1L;

        Friendship friendship1 = Friendship.builder()
                .userId(userId)
                .friendId(2L)
                .build();

        Friendship friendship2 = Friendship.builder()
                .userId(userId)
                .friendId(3L)
                .build();

        List<Friendship> friendships = List.of(
                friendship1,
                friendship2
        );

        List<User> users = List.of(
                User.builder().id(2L).username("Alice").build(),
                User.builder().id(3L).username("Bob").build()
        );

        when(friendshipRepository.findByUserId(userId)).thenReturn(friendships);
        when(userService.findAllById(Arrays.asList(2L, 3L))).thenReturn(users);

        List<UserResponse> result = friendshipService.getFriends(userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(2L);
        assertThat(result.get(1).username()).isEqualTo("Bob");

        verify(friendshipRepository).findByUserId(userId);
        verify(userService).findAllById(List.of(2L, 3L));
    }

    @Test
    void areFriends_returnsTrueIfFriendshipExists() {
        Long senderId = 1L;
        Long receiverId = 2L;

        when(friendshipRepository.existsByUserIdAndFriendId(senderId, receiverId)).thenReturn(true);

        boolean result = friendshipService.areFriends(senderId, receiverId);

        assertThat(result).isTrue();
        verify(friendshipRepository).existsByUserIdAndFriendId(senderId, receiverId);
    }

    @Test
    void removeFriend_removesFriendAndPublishesEvent() {
        Long userId = 1L;
        Long friendId = 2L;

        doNothing().when(friendshipManager).deleteBidirectional(userId, friendId, friendshipRepository);

        friendshipService.removeFriend(userId, friendId);

        verify(friendshipManager).deleteBidirectional(userId, friendId, friendshipRepository);
        verify(eventPublisher).publishEvent(any(FriendshipRemovedEvent.class));
    }

    @Test
    void removeFriend_handlesExceptionGracefully() {
        Long userId = 1L;
        Long friendId = 2L;

        doThrow(new RuntimeException("DB error")).when(friendshipManager)
                .deleteBidirectional(userId, friendId, friendshipRepository);

        friendshipService.removeFriend(userId, friendId);

        verify(friendshipManager).deleteBidirectional(userId, friendId, friendshipRepository);
        verify(eventPublisher, never()).publishEvent(any());
    }
}

