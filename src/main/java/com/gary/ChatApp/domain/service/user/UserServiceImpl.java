package com.gary.ChatApp.domain.service.user;

import com.gary.ChatApp.domain.model.user.User;
import com.gary.ChatApp.domain.repository.UserRepository;
import com.gary.ChatApp.domain.service.userPresenceService.UserPresenceService;
import com.gary.ChatApp.exceptions.DuplicateResourceException;
import com.gary.ChatApp.exceptions.UnauthorizedException;
import com.gary.ChatApp.security.JwtTokenUtil;
import com.gary.ChatApp.web.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserPresenceService userPresenceService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public String register(String username, String password) {

        if (userRepository.findByName(username).isPresent()) {
            throw new DuplicateResourceException("Username already exists");
        }

        userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build());

        return "User registered successfully";
    }

    @Override
    public LoginResponse login(String username, String password) {
        User user = userRepository.findByName(username)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername());

        userPresenceService.setOnline(user.getId());

        return LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .token(token)
                .build();
    }


    @Override
    public Optional<User> getById(Long id) {
        Optional<User> user = userRepository.findById(id);
        user.ifPresent(u -> u.setOnline(userPresenceService.isOnline(u.getId())));
        return user;
    }

    @Override
    public Optional<User> getByName(String name) {
        Optional<User> user = userRepository.findByName(name);
        user.ifPresent(u -> u.setOnline(userPresenceService.isOnline(u.getId())));
        return user;
    }

    @Override
    public void setOnlineStatus(Long userId, boolean online) {
        if (online) {
            userPresenceService.setOnline(userId);
        } else {
            userPresenceService.setOffline(userId);
        }
    }

    @Override
    public List<User> getOnlineUsers() {
        // Redis doesn't store all keys in a searchable way by default,
        // so we must get all users from DB and check each one's status.
        return userRepository.findAll().stream()
                .filter(user -> userPresenceService.isOnline(user.getId()))
                .peek(user -> user.setOnline(true))
                .collect(Collectors.toList());
    }
}
