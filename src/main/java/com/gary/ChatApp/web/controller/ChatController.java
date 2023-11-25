package com.gary.ChatApp.web.controller;

import com.gary.ChatApp.exceptions.FriendshipDoesnotExistException;
import com.gary.ChatApp.service.chatMessage.ChatMessageService;
import com.gary.ChatApp.service.friendRequest.FriendRequestService;
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


    @MessageMapping("/chat/{friendId}")
    @SendTo("/topic/chat/{friendId}")
    @CheckUserExistence
    public ChatMessage sendMessage(@Payload String content, @DestinationVariable("friendId") Long friendId) {

        Long senderId = UserContext.getUser().getId();
       return chatMessageService.save(chatMessageDTOMapper.fromDTO(
               content,
               senderId,
               friendId
       ));
    }
    //when chat will open between two persons

}
