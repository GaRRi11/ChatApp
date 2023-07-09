package com.gary.ChatApp.service.user;

import com.gary.ChatApp.storage.model.user.User;
import com.gary.ChatApp.storage.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public User save (User user){
        return userRepository.save(user);
    }

    public Optional<User> findByName (String name){
        return userRepository.findByName(name);
    }

    public List<User> getAll (){
        return userRepository.findAll();
    }
}
