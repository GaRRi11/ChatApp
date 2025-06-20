package com.gary.web.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class SendFriendRequestServiceUnavailableException extends RuntimeException {
  public SendFriendRequestServiceUnavailableException(String message) {
    super(message);
  }
}
