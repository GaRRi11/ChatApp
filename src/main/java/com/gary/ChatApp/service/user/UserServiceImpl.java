package com.gary.ChatApp.service.user;

import com.gary.ChatApp.storage.model.user.User;
import com.gary.ChatApp.storage.repository.UserRepository;
import com.gary.ChatApp.web.dto.UserRequest;
import com.gary.ChatApp.web.security.SessionManager;
import com.gary.ChatApp.web.security.UserAuthenticationManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final SessionManager sessionManager;
    private final UserAuthenticationManager userAuthenticationManager;

    public User save (User user){
        userRepository.save(user);
        return user;
    }

    public void authenticate(UserRequest request,HttpServletResponse response){
        if (!userAuthenticationManager.authenticate(request.getName(),request.getPassword())){
            throw new IllegalArgumentException("Password Is Incorrect");
        }
        sessionManager.createSession(userRepository.findByName(request.getName()).get().getId(),response);
    }

    public void logout (HttpServletRequest request,HttpServletResponse response){
        sessionManager.logout(request,response);
    }

    public Optional<User> findByName (String name){
        return userRepository.findByName(name);
    }

    public List<User> getAll (){
        return userRepository.findAll();
    }
}
