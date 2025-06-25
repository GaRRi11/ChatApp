package com.gary.application.friendship;


import com.gary.domain.repository.jpa.friendship.FriendshipRepository;
import com.gary.domain.model.friendship.Friendship;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FriendshipManager {

    private final FriendshipRepository repository;

    public void saveBidirectional(UUID userId1, UUID userId2) {
        List<Friendship> friendships = List.of(
                new Friendship(null, userId1, userId2),
                new Friendship(null, userId2, userId1)
        );
        repository.saveAll(friendships);
    }

    public void deleteBidirectional(UUID userId1, UUID userId2) {
        repository.deleteByUserIdAndFriendId(userId1, userId2);
        repository.deleteByUserIdAndFriendId(userId2, userId1);
    }
}

