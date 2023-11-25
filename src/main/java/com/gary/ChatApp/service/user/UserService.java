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

    User save(User user);

    Optional<User> findByName(String name);

    Optional<User> findById(Long Id);


    void authenticate(UserRequest request, HttpServletResponse response);

    void addFriend(Long receiverId,Long senderId);

    void deleteFriend(Long receiverId,Long senderId);


    void logout (HttpServletRequest request, HttpServletResponse response);

    void setUserOnlineStatus(User user, boolean onlineStatus);

    void updateLastSeen();

    List<User> getAll();

    List<User> getActiveUsers();
}
