package com.gary.exceptions;

public class FriendRequestNotFoundException extends RuntimeException {
    public FriendRequestNotFoundException(Long requestId) {
        super("Friend request not found for ID: " + requestId);
    }
}
