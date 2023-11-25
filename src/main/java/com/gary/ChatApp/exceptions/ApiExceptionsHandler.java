package com.gary.ChatApp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ApiExceptionsHandler {
    @ExceptionHandler(value = {FriendshipAlreadyExistsException.class})
    public ResponseEntity<Object> handleFriendshipAlreadyExistsException (FriendshipAlreadyExistsException e){
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        ApiException apiException = new ApiException(
                e.getMessage(),
                badRequest,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(apiException, badRequest);
    }
    @ExceptionHandler(value = {FriendshipDoesnotExistException.class})
    public ResponseEntity<Object> handleFriendshipDoesnotExistException (FriendshipDoesnotExistException e){
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        ApiException apiException = new ApiException(
                e.getMessage(),
                badRequest,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(apiException, badRequest);
    }
    @ExceptionHandler(value = {UserDoesnotExistException.class})
    public ResponseEntity<Object> handleUserDoesnotExistException (UserDoesnotExistException e){
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        ApiException apiException = new ApiException(
                e.getMessage(),
                badRequest,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(apiException, badRequest);
    }
}
