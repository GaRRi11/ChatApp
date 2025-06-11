package com.gary.domain.service.presence;

import org.springframework.stereotype.Service;

public interface UserPresenceService {
    void refreshOnlineStatus(Long userId);
    void setOffline(Long userId);
    boolean isOnline(Long userId);
}

