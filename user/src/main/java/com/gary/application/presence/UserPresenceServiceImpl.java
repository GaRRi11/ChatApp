package com.gary.application.presence;

import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.domain.repository.cache.presence.UserPresenceCacheRepository;
import com.gary.domain.service.presence.UserPresenceService;
import com.gary.domain.service.user.UserService;
import com.gary.web.dto.cache.presence.UserPresenceCacheDto;
import com.gary.web.exception.rest.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserPresenceServiceImpl implements UserPresenceService {

    private final UserPresenceCacheRepository userPresenceCacheRepository;
    private final UserService userService;

    @Value("${presence.status.online}")
    private String onlineStatus;


    @Override
    @LoggableAction("Refresh Online Status")
    @Timed("presence.RefreshOnline.duration")
    public void refreshOnlineStatus(UUID userId) {

        UserPresenceCacheDto userPresenceCacheDto = UserPresenceCacheDto.builder()
                .userId(userId)
                .status(onlineStatus)
                .build();

        userPresenceCacheRepository.save(userPresenceCacheDto);

    }


    @Override
    @LoggableAction("Set Offline Status")
    @Timed("presence.setOffline.duration")
    public void setOffline(UUID userId) {
        userPresenceCacheRepository.deleteById(userId);
    }


    @Override
    @LoggableAction("Check Online Status")
    public boolean isOnline(UUID userId) {

        if (userService.findById(userId).isEmpty()){
            log.debug("User with ID {} not found.", userId);
            throw new ResourceNotFoundException("User with ID " + userId + " not found");
        }

        return userPresenceCacheRepository.existsById(userId);
    }

}
