package com.gary.web.exception;

public class RateLimiterServiceUnavailableException extends RuntimeException {
    public RateLimiterServiceUnavailableException(String message) {
        super(message);
    }
}

