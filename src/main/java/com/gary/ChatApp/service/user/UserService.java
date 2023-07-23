package com.gary.ChatApp.service.user;

import com.gary.ChatApp.storage.model.user.User;
import com.gary.ChatApp.web.dto.UserRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface UserService {

    User save(User user, HttpServletResponse response);

    Optional<User> findByName(String name);

    void authenticate(UserRequest request, HttpServletResponse response);

    void logout (HttpServletRequest request, HttpServletResponse response);

    List<User> getAll();
}
