package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.service.chatMessage.ChatMessageService;
import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
import com.gary.ChatApp.web.dto.ChatMessageDTOMapper;
import com.gary.ChatApp.web.dto.ChatMessageRequest;
import com.gary.ChatApp.web.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ChatMessageDTOMapper chatMessageDTOMapper;

    @PostMapping("/send")
    public ResponseEntity<String> save(@RequestBody ChatMessageRequest chatMessageRequest){
        chatMessageRequest.setSender(UserContext.getUser().getName());
        chatMessageService.save(chatMessageDTOMapper.fromDTO(chatMessageRequest));
        return ResponseEntity.ok("Message Sent");
    }

    @GetMapping("/all")
    public ResponseEntity<List<ChatMessage>> getAll (){
        return ResponseEntity.ok(chatMessageService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatMessage> hi (@PathVariable("id") Long id){
        return ResponseEntity.ok(chatMessageService.findById(id));
    }


    @MessageMapping("chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage){
        return chatMessage;
    }

    @MessageMapping("chat.addUser")
    @SendTo("topic/public")
    public ChatMessage addUser(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor){
        headerAccessor.getSessionAttributes().put("username",chatMessage.getSender());
        return chatMessage;
    }

    @MessageMapping("/chat/{friendId}")
    @SendTo("/topic/chat/{friendId}")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage, @DestinationVariable("friendId") String friendId) {
        // Save message to database or perform necessary actions
        return chatMessage;
    }



}
