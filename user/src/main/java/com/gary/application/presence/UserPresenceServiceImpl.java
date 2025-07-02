package com.gary.application.presence;

import com.gary.common.annotations.LoggableAction;
import com.gary.common.annotations.Timed;
import com.gary.common.metric.MetricIncrement;
import com.gary.common.time.TimeFormat;
import com.gary.domain.repository.cache.presence.UserPresenceCacheRepository;
import com.gary.domain.service.presence.UserPresenceService;
import com.gary.web.dto.cache.presence.UserPresenceCacheDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserPresenceServiceImpl implements UserPresenceService {

    private final UserPresenceCacheRepository userPresenceCacheRepository;

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
        return userPresenceCacheRepository.existsById(userId);
    }

}
