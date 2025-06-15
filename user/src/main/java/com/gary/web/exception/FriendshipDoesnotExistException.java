package com.gary.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FriendshipDoesnotExistException extends RuntimeException {
    public FriendshipDoesnotExistException (UUID senderId, UUID recieverId) {
        super("Friendship between user:" + senderId + "and " + recieverId + "Does not exist");
    }
}
