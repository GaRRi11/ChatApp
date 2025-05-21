//package com.gary.ChatApp.web.controller;
//
//import com.gary.ChatApp.domain.service.chatMessage.ChatMessageService;
//import com.gary.ChatApp.domain.model.chatmessage.ChatMessage;
//import com.gary.ChatApp.web.dto.ChatMessageDTOMapper;
//import com.gary.ChatApp.web.security.UserContext;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.messaging.handler.annotation.DestinationVariable;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/chat")
//@RequiredArgsConstructor
//public class ChatController {
//
//    private final ChatMessageService chatMessageService;
//    private final ChatMessageDTOMapper chatMessageDTOMapper;
//    private final SimpMessagingTemplate messagingTemplate;
//
//
//
//    @MessageMapping("/chat/{friendId}")
//    @SendTo("/topic/chat/{friendId}")
//    @CheckUserExistence
//    public ChatMessage sendMessage(@Payload String content, @DestinationVariable("friendId") Long friendId) {
//
//        Long senderId = UserContext.getUser().getId();
//        ChatMessage savedMesage = chatMessageService.save(chatMessageDTOMapper.fromDTO(content, senderId, friendId));
//        messagingTemplate.convertAndSend("/topic/chat/" + friendId, savedMesage);
//        return savedMesage;
//
//    }
//    @GetMapping("/history/{friendId}")
//    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable("friendId") Long friendId) {
//        Long currentUserId = UserContext.getUser().getId();
//        List<ChatMessage> chatHistory = chatMessageService.getChatMessagesBetweenTwoUsers(currentUserId, friendId);
//        return ResponseEntity.ok(chatHistory);
//    }
//
//
//}
