package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.exceptions.UserDoesnotExistException;
import com.gary.ChatApp.service.user.UserService;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@AllArgsConstructor
public class FriendControllerAspect {

    private final UserService userService;


    @Around("@annotation(com.gary.ChatApp.web.controller.CheckUserExistence)")
    public Object checkIfUserExists(ProceedingJoinPoint joinPoint) throws Throwable {
        Long userId = extractReceiverId(joinPoint.getArgs());
        if (userService.findById(userId).isEmpty()){
            throw new UserDoesnotExistException(userId);
        }
        return joinPoint.proceed();
    }

    private Long extractReceiverId(Object[] args) {
        // Assuming the receiverId is the second argument in the method signature
        // Modify this logic based on the actual structure of your method arguments

        if (args.length > 1 && args[1] instanceof Long) {
            return (Long) args[1];
        }

        return null; // Return null or handle missing receiverId scenario
    }
}
