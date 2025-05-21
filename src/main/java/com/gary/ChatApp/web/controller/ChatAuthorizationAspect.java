//package com.gary.ChatApp.web.controller;
//
//import com.gary.ChatApp.exceptions.FriendshipDoesnotExistException;
//import com.gary.ChatApp.domain.service.friendRequest.FriendRequestService;
//import com.gary.ChatApp.web.security.UserContext;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Aspect
//@Component
//public class ChatAuthorizationAspect {
//
//    @Autowired
//    private FriendRequestService friendRequestService;
//
//    @Around("@annotation(org.springframework.messaging.handler.annotation.MessageMapping)")
//    public Object checkFriendship(ProceedingJoinPoint joinPoint) throws Throwable {
//
//        Long senderId = UserContext.getUser().getId(); // Extract senderId
//        Long friendId = extractReceiverId(joinPoint.getArgs()); // Extract friendId
//
//        if (!friendRequestService.checkFriendStatus(senderId, friendId)) {
//            throw new FriendshipDoesnotExistException(senderId, friendId);
//        }
//
//        return joinPoint.proceed(); // Proceed with the original method execution
//    }
//    private Long extractReceiverId(Object[] args) {
//        // Assuming the receiverId is the second argument in the method signature
//        // Modify this logic based on the actual structure of your method arguments
//
//        if (args.length > 1 && args[1] instanceof Long) {
//            return (Long) args[1];
//        }
//
//        return null; // Return null or handle missing receiverId scenario
//    }
//}
