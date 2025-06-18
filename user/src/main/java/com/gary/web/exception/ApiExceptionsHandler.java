package com.gary.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

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

    @ExceptionHandler(RespondToRequestServiceUnavailableException.class)
    public ResponseEntity<String> handleRespondToRequestServiceUnavailableException(RespondToRequestServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Server is temporarily unable to process your request. Please try again later.");
    }



    @ExceptionHandler(MessagePersistenceException.class)
    public ResponseEntity<String> handleMessagePersistenceException(MessagePersistenceException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Server is temporarily unable to process your request. Please try again later.");
    }


    @ExceptionHandler(RateLimiterServiceUnavailableException.class)
    public ResponseEntity<String> handleRateLimiterUnavailable(RateLimiterServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Server is temporarily unable to process your request. Please try again later.");
    }


    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateResourceException(DuplicateResourceException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
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
