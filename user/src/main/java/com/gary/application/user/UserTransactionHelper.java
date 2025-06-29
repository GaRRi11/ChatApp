package com.gary.application.user;

import com.gary.domain.model.user.User;
import com.gary.domain.repository.jpa.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserTransactionHelper {

    private final UserRepository userRepository;

    @Transactional(
            readOnly = true,
            propagation = Propagation.SUPPORTS)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation = Isolation.SERIALIZABLE)
    public User save(User user) {
        return userRepository.save(user);
    }


    @Transactional(
            readOnly = true,
            propagation = Propagation.SUPPORTS)
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }


    @Transactional(
            readOnly = true,
            propagation = Propagation.SUPPORTS)
    public List<User> findAllById(List<UUID> userIds) {
        return userRepository.findAllById(userIds);
    }




}
