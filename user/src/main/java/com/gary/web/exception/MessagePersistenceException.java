package com.gary.web.exception;

public class MessagePersistenceException extends RuntimeException {
    public MessagePersistenceException(String message) {
        super(message);
    }

    public MessagePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}

