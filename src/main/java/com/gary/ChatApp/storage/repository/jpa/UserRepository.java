package com.gary.ChatApp.storage.repository.jpa;

import com.gary.ChatApp.storage.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    @Query("SELECT s FROM User s WHERE s.name = ?1")
    Optional<User> findByName (String email);

}
