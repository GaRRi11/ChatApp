package com.gary.ChatApp.domain.model.user;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(unique = true, nullable = false)
    private String token;

    private boolean revoked = false;

    private Long expiryDate;  // timestamp in millis
}
