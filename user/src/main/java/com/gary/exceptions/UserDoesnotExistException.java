package com.gary.exceptions;

public class UserDoesnotExistException extends RuntimeException {

    public UserDoesnotExistException (Long userId) {
        super("User With Id:" + userId + "Does Not Exist");
    }
}
