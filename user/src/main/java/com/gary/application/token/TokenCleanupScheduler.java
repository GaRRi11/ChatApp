package com.gary.application.token;

import com.gary.common.annotations.LoggableAction;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenServiceImpl tokenService;

    @Scheduled(fixedDelay = 86_400_000)
    public void cleanExpiredTokens() {
        tokenService.clearExpiredTokens();
    }
}

