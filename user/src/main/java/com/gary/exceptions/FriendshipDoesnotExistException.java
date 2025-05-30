package com.gary.exceptions;

public class FriendshipDoesnotExistException extends RuntimeException {
    public FriendshipDoesnotExistException (Long senderId,Long recieverId) {
        super("Friendship between user:" + senderId + "and " + recieverId + "Does not exist");
    }
}
