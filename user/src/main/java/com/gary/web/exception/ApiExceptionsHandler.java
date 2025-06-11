package com.gary.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@RestControllerAdvice
public class ApiExceptionsHandler {

    private ResponseEntity<Object> buildResponse(RuntimeException e, HttpStatus status) {
        ApiException apiException = new ApiException(
                e.getMessage(),
                status,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(apiException, status);
    }

    @ExceptionHandler(FriendshipAlreadyExistsException.class)
    public ResponseEntity<Object> handleFriendshipAlreadyExists(FriendshipAlreadyExistsException e) {
        return buildResponse(e, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(FriendshipDoesnotExistException.class)
    public ResponseEntity<Object> handleFriendshipDoesNotExist(FriendshipDoesnotExistException e) {
        return buildResponse(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserDoesnotExistException.class)
    public ResponseEntity<Object> handleUserDoesNotExist(UserDoesnotExistException e) {
        return buildResponse(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FriendRequestNotFoundException.class)
    public ResponseEntity<Object> handleFriendRequestNotFound(FriendRequestNotFoundException e) {
        return buildResponse(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Object> handleTooManyRequests(TooManyRequestsException e) {
        return buildResponse(e, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorized(UnauthorizedException e) {
        return buildResponse(e, HttpStatus.UNAUTHORIZED);
    }
}
