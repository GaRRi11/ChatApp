package com.gary.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserDoesnotExistException extends RuntimeException {
    public UserDoesnotExistException (UUID userId) {
        super("User With Id:" + userId + "Does Not Exist");
    }
}
