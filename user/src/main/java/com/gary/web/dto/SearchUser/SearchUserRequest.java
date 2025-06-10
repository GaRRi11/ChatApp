package com.gary.web.dto.SearchUser;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SearchUserRequest(


        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
        String username
) {
}
