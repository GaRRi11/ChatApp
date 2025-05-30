package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.service.presence.UserPresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/presence")
@RequiredArgsConstructor
public class UserPresenceController {

    private final UserPresenceService userPresenceService;

    @GetMapping("/is-online/{userId}")
    public boolean isOnline(@PathVariable Long userId) {
        return userPresenceService.isOnline(userId);
    }
}
