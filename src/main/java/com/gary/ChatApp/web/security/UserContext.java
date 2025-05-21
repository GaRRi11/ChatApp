package com.gary.ChatApp.web.security;

import com.gary.ChatApp.domain.model.user.User;

public class UserContext {

    private static  ThreadLocal<User> userThreads = new ThreadLocal<>();

    public static User getUser() {
        return userThreads.get();
    }

    public static void setUser(User user) {
        userThreads.set(user);
    }
}
