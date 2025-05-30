package com.gary.domain.repository;

import com.gary.ChatApp.domain.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);

    List<User> findByOnlineTrue();
}
