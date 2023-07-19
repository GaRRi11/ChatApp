package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.service.chatMessage.ChatMessageService;
import com.gary.ChatApp.storage.model.chatmessage.ChatMessage;
import com.gary.ChatApp.web.dto.ChatMessageDTOMapper;
import com.gary.ChatApp.web.dto.ChatMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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



}
