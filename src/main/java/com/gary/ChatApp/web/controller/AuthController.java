package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.service.user.UserService;
import com.gary.ChatApp.web.dto.UserDTOMapper;
import com.gary.ChatApp.web.dto.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    private final UserDTOMapper userDTOMapper;

    @PostMapping("/register")
    public ResponseEntity<String> register (@RequestBody UserRequest userRequest){

        if (userRequest.getName() == null){
            throw new NullPointerException("The request was malformed or missing required fields");
        }

        if (userService.findByName(userRequest.getName()).isPresent()){
            return ResponseEntity.badRequest().body("Username already exists");
        }

        userService.save(userDTOMapper.fromDTO(userRequest));

        return ResponseEntity.ok("User registered successfully");
    }

    @GetMapping("/login")
    public ResponseEntity<String> hi(){
        return ResponseEntity.ok("hi");
    }
}
