package com.gary.web.controller.user;

import com.gary.domain.service.presence.UserPresenceService;
import com.gary.domain.service.user.UserService;
import com.gary.web.dto.userPresenceResponse.UserPresenceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/presence")
@RequiredArgsConstructor
@Slf4j
public class UserPresenceController {

    private final UserPresenceService userPresenceService;
    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserPresenceResponse> isOnline(@PathVariable UUID userId) {
        boolean online = userPresenceService.isOnline(userId);
        return ResponseEntity.ok(new UserPresenceResponse(online));
    }
}
