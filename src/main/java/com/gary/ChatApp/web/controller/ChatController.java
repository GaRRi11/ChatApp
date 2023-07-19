package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.service.chatMessage.ChatMessageService;
import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
import com.gary.ChatApp.storage.repository.chatMessage.ChatMessageRepository;
import com.gary.ChatApp.web.dto.ChatMessageDTOMapper;
import com.gary.ChatApp.web.dto.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
@EnableCaching
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ChatMessageDTOMapper chatMessageDTOMapper;

    @PostMapping("/send")
    public ResponseEntity<String> save(@RequestBody ChatMessageRequest chatMessageRequest){
        chatMessageService.save(chatMessageDTOMapper.fromDTO(chatMessageRequest));
        return ResponseEntity.ok("Message Sent");
    }

    @GetMapping("/all")
    @Cacheable(cacheNames = "allChatMessages")
    public ResponseEntity<List<ChatMessage>> getAll (){
//        return ResponseEntity.ok(chatMessageService.getAll() );
        return ResponseEntity.ok(chatMessageService.getAll());
    }

    @GetMapping("/hi")
    public ResponseEntity<String> hi (){
        return ResponseEntity.ok("hi");
    }



}
