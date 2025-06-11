
import com.gary.application.friendRequest.FriendRequestServiceImpl;
import com.gary.domain.event.friendRequestAccepted.FriendRequestAcceptedEvent;
import com.gary.domain.model.friendrequest.FriendRequest;
import com.gary.domain.model.friendrequest.RequestStatus;
import com.gary.domain.repository.friendRequest.FriendRequestRepository;
import com.gary.web.dto.friendRequest.FriendRequestResponse;
import com.gary.web.dto.respondToFriendDto.RespondToFriendDto;
import com.gary.web.exception.DuplicateResourceException;
import com.gary.web.exception.FriendRequestNotFoundException;
import com.gary.web.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FriendRequestServiceImplTest {

    private FriendRequestRepository friendRequestRepository;
    private ApplicationEventPublisher eventPublisher;
    private FriendRequestServiceImpl friendRequestService;

    @BeforeEach
    void setUp() {
        friendRequestRepository = mock(FriendRequestRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        friendRequestService = new FriendRequestServiceImpl(friendRequestRepository, eventPublisher);
    }

    @Test
    void sendRequest_shouldSaveNewRequest_whenNotExists() {
        Long senderId = 1L;
        Long receiverId = 2L;

        when(friendRequestRepository.existsBySenderIdAndReceiverIdAndStatusIn(eq(senderId), eq(receiverId), any()))
                .thenReturn(false);

        when(friendRequestRepository.save(any(FriendRequest.class))).thenAnswer(i -> i.getArgument(0));

        FriendRequestResponse response = friendRequestService.sendRequest(senderId, receiverId);

        assertThat(response.senderId()).isEqualTo(senderId);
        assertThat(response.receiverId()).isEqualTo(receiverId);
    }

    @Test
    void sendRequest_shouldThrowException_whenRequestAlreadyExists() {
        when(friendRequestRepository.existsBySenderIdAndReceiverIdAndStatusIn(anyLong(), anyLong(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> friendRequestService.sendRequest(1L, 2L))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void respondToRequest_shouldAcceptRequest_andPublishEvent() {
        Long requestId = 10L;
        Long receiverId = 2L;
        FriendRequest request = FriendRequest.builder()
                .id(requestId)
                .senderId(1L)
                .receiverId(receiverId)
                .status(RequestStatus.PENDING)
                .build();

        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        RespondToFriendDto dto = new RespondToFriendDto(requestId, true);

        friendRequestService.respondToRequest(dto, receiverId);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.ACCEPTED);
        verify(friendRequestRepository).save(request);
        verify(eventPublisher).publishEvent(any(FriendRequestAcceptedEvent.class));
    }

    @Test
    void respondToRequest_shouldDeclineRequest_andNotPublishEvent() {
        Long requestId = 11L;
        Long receiverId = 3L;
        FriendRequest request = FriendRequest.builder()
                .id(requestId)
                .senderId(1L)
                .receiverId(receiverId)
                .status(RequestStatus.PENDING)
                .build();

        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        RespondToFriendDto dto = new RespondToFriendDto(requestId, false);

        friendRequestService.respondToRequest(dto, receiverId);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.DECLINED);
        verify(friendRequestRepository).save(request);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void respondToRequest_shouldThrowUnauthorizedException_whenNotReceiver() {
        FriendRequest request = FriendRequest.builder()
                .id(12L)
                .senderId(1L)
                .receiverId(2L)
                .status(RequestStatus.PENDING)
                .build();

        when(friendRequestRepository.findById(12L)).thenReturn(Optional.of(request));

        RespondToFriendDto dto = new RespondToFriendDto(12L, true);

        assertThatThrownBy(() -> friendRequestService.respondToRequest(dto, 3L))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void respondToRequest_shouldThrowNotFoundException_whenRequestNotExists() {
        when(friendRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        RespondToFriendDto dto = new RespondToFriendDto(13L, true);

        assertThatThrownBy(() -> friendRequestService.respondToRequest(dto, 2L))
                .isInstanceOf(FriendRequestNotFoundException.class);
    }

    @Test
    void getPendingRequests_shouldReturnPendingRequests() {
        when(friendRequestRepository.findByReceiverIdAndStatus(1L, RequestStatus.PENDING))
                .thenReturn(List.of(FriendRequest.builder()
                        .senderId(2L)
                        .receiverId(1L)
                        .status(RequestStatus.PENDING)
                        .build()));

        List<FriendRequestResponse> result = friendRequestService.getPendingRequests(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).receiverId()).isEqualTo(1L);
    }

    @Test
    void getSentRequests_shouldReturnSentRequests() {
        when(friendRequestRepository.findBySenderIdAndStatus(2L, RequestStatus.PENDING))
                .thenReturn(List.of(FriendRequest.builder()
                        .senderId(2L)
                        .receiverId(3L)
                        .status(RequestStatus.PENDING)
                        .build()));

        List<FriendRequestResponse> result = friendRequestService.getSentRequests(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).senderId()).isEqualTo(2L);
    }
}
