package com.gary.config;


public class RedisKeys {

    public static String userPresence(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        return "user:presence:" + userId;
    }

    public static String chatMessages(Long user1Id, Long user2Id) {
        if (user1Id == null || user2Id == null) throw new IllegalArgumentException("userIds cannot be null");
        long first = Math.min(user1Id, user2Id);
        long second = Math.max(user1Id, user2Id);
        return "chat:messages:" + first + ":" + second;
    }

    public static String messageRateLimit(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        return "rate:message:" + userId;
    }
}

