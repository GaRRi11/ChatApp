package com.gary.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class MessagePersistenceException extends RuntimeException {
    public MessagePersistenceException(String message) {
        super(message);
    }

    public MessagePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}

