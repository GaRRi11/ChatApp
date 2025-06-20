package com.gary.web.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class GetFriendRequestsServiceUnavailableException extends RuntimeException {
    public GetFriendRequestsServiceUnavailableException(String message) {
        super(message);
    }
}
