package com.gary.ChatApp.web.security;

import com.gary.ChatApp.storage.model.user.User;

public class UserContext {

    private static  ThreadLocal<User> usernameThreads = new ThreadLocal<>();

    public static User getUser() {
        return usernameThreads.get();
    }

    public static void setUser(User user) {
        usernameThreads.set(user);
    }
}
