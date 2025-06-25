package com.gary.domain.repository.jpa.user;

import com.gary.domain.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String username);

}
