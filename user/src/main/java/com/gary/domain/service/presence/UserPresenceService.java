package com.gary.domain.service.presence;


public interface UserPresenceService {
    void refreshOnlineStatus(Long userId);
    void setOffline(Long userId);
    boolean isOnline(Long userId);
}

