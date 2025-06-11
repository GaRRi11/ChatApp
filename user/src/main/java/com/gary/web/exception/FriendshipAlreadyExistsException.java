package com.gary.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class FriendshipAlreadyExistsException extends RuntimeException{
    public FriendshipAlreadyExistsException(Long senderId,Long recieverId) {
        super("Friendship between user:" + senderId + "and " + recieverId + "already exists");
    }
}
