package com.gary.infrastructure.constants;


import java.util.UUID;

public class RedisKeys {

    public static String userPresence(UUID userId) {
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        return "user:presence:" + userId;
    }

    public static String chatMessages(UUID user1Id, UUID user2Id) {
        if (user1Id == null || user2Id == null) throw new IllegalArgumentException("userIds cannot be null");
        String first = user1Id.toString().compareTo(user2Id.toString()) < 0 ? user1Id.toString() : user2Id.toString();
        String second = user1Id.toString().compareTo(user2Id.toString()) >= 0 ? user1Id.toString() : user2Id.toString();
        return "chat:messages:" + first + ":" + second;
    }

    public static String messageRateLimit(UUID userId) {
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        return "rate:message:" + userId;
    }

    public static String refreshToken(String token) {
        if (token == null) throw new IllegalArgumentException("token cannot be null");
        return "auth:refresh:" + token;
    }

    public static String refreshTokenSet(UUID userId) {
        if (userId == null) throw new IllegalArgumentException("userId cannot be null");
        return "auth:refresh:set:" + userId;
    }
}


