package com.gary.ChatApp.domain.service.userPresenceService;

import org.springframework.stereotype.Service;

@Service
public interface UserPresenceService {
    void refreshOnlineStatus(Long userId);
    void setOffline(Long userId);
    boolean isOnline(Long userId);
}

