package com.gary.domain.service.presence;


import java.util.UUID;

public interface UserPresenceService {
    void refreshOnlineStatus(UUID userId);
    void setOffline(UUID userId);
    boolean isOnline(UUID userId);
    void refreshOnlineStatusFallback(UUID userId, Throwable t);
    void setOfflineFallback(UUID userId, Throwable t);
    boolean isOnlineFallback(UUID userId, Throwable t);

}

