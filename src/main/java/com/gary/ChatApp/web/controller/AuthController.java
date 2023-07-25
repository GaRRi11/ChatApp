package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.service.user.UserService;
import com.gary.ChatApp.web.dto.UserDTOMapper;
import com.gary.ChatApp.web.dto.UserRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    private final UserDTOMapper userDTOMapper;

    @PostMapping("/register")
    public ModelAndView register (@RequestBody UserRequest userRequest, HttpServletResponse response){

        if (userRequest.getName() == null){
            throw new NullPointerException("The request was malformed or missing required fields");
        }

        if (userService.findByName(userRequest.getName()).isPresent()){
            throw new IllegalArgumentException("Username already exists");
        }

        userService.save(userDTOMapper.fromDTO(userRequest),response);

        return new ModelAndView("redirect:/chat/all");
    }

    @PostMapping("/login")
    public ModelAndView authenticate(@RequestBody UserRequest request,HttpServletResponse response) {
        if (
                request.getName() == null ||
                        request.getPassword() == null
        ) {
            throw new NullPointerException("The request was malformed or missing required fields");
        }
        userService.authenticate(request,response);
        return new ModelAndView("redirect:/chat/all");

    }

    @GetMapping("/logout")
    public ModelAndView logout(HttpServletRequest request,HttpServletResponse response) {
        userService.logout(request,response);
        return new ModelAndView("redirect:/login");
    }

}
