package com.gary.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class FriendshipAlreadyExistsException extends RuntimeException{
    public FriendshipAlreadyExistsException(UUID senderId, UUID recieverId) {
        super("Friendship between user:" + senderId + "and " + recieverId + "already exists");
    }
}
