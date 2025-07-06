package com.gary.domain.model.token;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {


    @Id
    @GeneratedValue()
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(unique = true, nullable = false)
    private UUID userId;

    @Column(unique = true, nullable = false)
    private String token;

    private Long expiryDate;
}
