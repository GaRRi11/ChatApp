package com.gary.web.controller.user;

import com.gary.domain.service.presence.UserPresenceService;
import com.gary.web.dto.userPresenceResponse.UserPresenceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/presence")
@RequiredArgsConstructor
public class UserPresenceController {

    private final UserPresenceService userPresenceService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserPresenceResponse> isOnline(@PathVariable Long userId) {
        boolean online = userPresenceService.isOnline(userId);
        return ResponseEntity.ok(new UserPresenceResponse(online));
    }
}
