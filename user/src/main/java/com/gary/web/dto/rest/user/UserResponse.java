package com.gary.web.dto.rest.user;

import com.gary.domain.model.user.User;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        LocalDateTime createdAt
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getCreatedAt()
        );
    }
}
