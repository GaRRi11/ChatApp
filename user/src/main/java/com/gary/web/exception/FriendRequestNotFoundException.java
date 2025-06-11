package com.gary.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FriendRequestNotFoundException extends RuntimeException {
    public FriendRequestNotFoundException(Long requestId) {
        super("Friend request not found for ID: " + requestId);
    }
}
