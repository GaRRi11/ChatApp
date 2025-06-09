package com.gary.web.dto.user;

import com.gary.domain.model.user.User;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
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
