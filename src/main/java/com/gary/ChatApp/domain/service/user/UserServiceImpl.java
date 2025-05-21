package com.gary.ChatApp.domain.service.user;

import com.gary.ChatApp.domain.model.user.User;
import com.gary.ChatApp.domain.repository.UserRepository;
import com.gary.ChatApp.web.dto.UserRequest;
import com.gary.ChatApp.web.security.SessionManager;
import com.gary.ChatApp.web.security.UserAuthenticationManager;
import com.gary.ChatApp.web.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        updateLastSeen();
    }

    @Override
    public void addFriend(Long receiverId, Long senderId) {
        User reciever = findById(receiverId).get(); //TODO mivxedav
        User sender  = findById(senderId).get();
        if (reciever != null && sender != null){
            List<Long> recieverFriendList = reciever.getFriendsIdList();
            recieverFriendList.add(senderId);
            reciever.setFriendsIdList(recieverFriendList);

            List<Long> senderFriendList = sender.getFriendsIdList();
            senderFriendList.add(receiverId);
            sender.setFriendsIdList(senderFriendList);

            save(reciever);
            save(sender);
        }

    }

    @Override
    public void deleteFriend(Long receiverId, Long senderId) {
        User reciever = findById(receiverId).get(); //TODO mivxedav
        User sender  = findById(senderId).get();
        if (reciever != null && sender != null){
            List<Long> receiverFriendList = reciever.getFriendsIdList();
            receiverFriendList.remove(senderId);
            reciever.setFriendsIdList(receiverFriendList);


            List<Long> senderFriendList = sender.getFriendsIdList();
            senderFriendList.remove(receiverId);
            sender.setFriendsIdList(senderFriendList);

            save(reciever);
            save(sender);
        }
        }

    public void logout (HttpServletRequest request,HttpServletResponse response){
        sessionManager.logout(request,response);
    }

    @Override
    @Scheduled(fixedRate = 60000) // Runs every minute
    public void updateUserStatus() {
        User user = UserContext.getUser();
        if (user!=null){
            LocalDateTime lastseen = user.getLastSeen();
            LocalDateTime now = LocalDateTime.now();
            if (lastseen != null && lastseen.isBefore(now.minusMinutes(1))){
                user.setOnline(false);
            }else {
                user.setOnline(true);
            }
        }
    }

    @Override
    public void updateUserOnlineStatus(User user, boolean online) {
        user.setOnline(online);
        save(user);
    }

    @Override
    public void updateLastSeen() {
        User user = UserContext.getUser();
        if (user != null){
            user.setLastSeen(LocalDateTime.now());
            save(user);
        }
    }

    public Optional<User> findByName (String name){
        return userRepository.findByName(name);
    }

    @Override
    public Optional<User> findById(Long Id) {
        return userRepository.findById(Id);
    }

    public List<User> getAll (){
        return userRepository.findAll();
    }



    @Override
    public List<User> getActiveUsers() {
        List<User> allUsers = getAll();
        List<User> activeUsers = new ArrayList<>();
        for (User allUser : allUsers) {
            if (allUser.isOnline()) {
                activeUsers.add(allUser);
            }
        }
        return activeUsers;
    }


}
