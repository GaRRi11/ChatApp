package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.service.user.UserService;
import com.gary.ChatApp.domain.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("all/actives")
    public ResponseEntity<List<User>> getActiveUsers(){
        return ResponseEntity.ok(userService.getActiveUsers());
    }


}
