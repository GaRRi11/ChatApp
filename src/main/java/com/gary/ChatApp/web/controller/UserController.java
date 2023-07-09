package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.service.user.UserService;
import com.gary.ChatApp.service.user.UserServiceImpl;
import com.gary.ChatApp.storage.model.user.User;
import com.gary.ChatApp.web.dto.UserDTOMapper;
import com.gary.ChatApp.web.dto.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
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

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.ok(userService.getAll());
    }


}
