package com.gary.ChatApp.domain.service.Friendship;

import com.gary.ChatApp.domain.model.friendrequest.Friendship;
import com.gary.ChatApp.repository.FriendshipRepository;
import com.gary.ChatApp.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;

    @Override
    public List<Long> getFriendIds(Long userId) {
        return friendshipRepository.findByUserId(userId).stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toList());
    }

    @Override
    public boolean areFriends(Long userId, Long otherUserId) {
        return friendshipRepository.findByUserIdAndFriendId(userId, otherUserId).isPresent();
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        friendshipRepository.deleteByUserIdAndFriendId(userId, friendId);
        friendshipRepository.deleteByFriendIdAndUserId(userId, friendId);
    }
}
