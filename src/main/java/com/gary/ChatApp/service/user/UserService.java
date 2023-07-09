package com.gary.ChatApp.service.user;

import com.gary.ChatApp.storage.model.user.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User save(User user);

    Optional<User> findByName(String name);

    List<User> getAll();
}
