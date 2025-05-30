package com.gary.exceptions;

public class FriendshipAlreadyExistsException extends RuntimeException{
    public FriendshipAlreadyExistsException(Long senderId,Long recieverId) {
        super("Friendship between user:" + senderId + "and " + recieverId + "already exists");
    }
}
