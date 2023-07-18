package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.service.chatMessage.ChatMessageService;
import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
import com.gary.ChatApp.web.dto.ChatMessageDTOMapper;
import com.gary.ChatApp.web.dto.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ChatMessageDTOMapper chatMessageDTOMapper;


    @PostMapping("/send")
    public ResponseEntity<String> save(@RequestBody ChatMessageRequest chatMessageRequest){
        chatMessageService.save(chatMessageDTOMapper.fromDTO(chatMessageRequest));
        return ResponseEntity.ok("Message Sent");
    }

    @GetMapping("/all")
    public List<ChatMessage> getAll (){
        return chatMessageService.getAll();
    }



}
