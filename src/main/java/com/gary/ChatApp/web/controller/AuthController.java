package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.domain.service.user.UserService;
import com.gary.ChatApp.web.dto.UserDTOMapper;
import com.gary.ChatApp.web.dto.UserRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    private final UserDTOMapper userDTOMapper;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRequest userRequest) {

        if (userRequest.getName() == null) {
            throw new NullPointerException("The request was malformed or missing required fields");
        }

        if (userService.findByName(userRequest.getName()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        userService.save(userDTOMapper.fromDTO(userRequest));
        log.info("registered user: {}", userRequest.getName());

        return ResponseEntity.ok("registered");

    }

    @PostMapping("/login")
    public ResponseEntity<String> authenticate(@RequestBody UserRequest request, HttpServletResponse response) {
        if (
                request.getName() == null ||
                        request.getPassword() == null
        ) {
            throw new NullPointerException("The request was malformed or missing required fields");
        }
        userService.authenticate(request, response);
        log.info("user logged: {}",request.getName());
        return ResponseEntity.ok("logged in");

    }

    //logout happens from frontend

}
