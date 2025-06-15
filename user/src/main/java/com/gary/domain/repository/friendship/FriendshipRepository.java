package com.gary.domain.repository.friendship;

import com.gary.domain.model.friendship.Friendship;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    List<Friendship> findByUserId(UUID userId);

    boolean existsByUserIdAndFriendId(UUID userId, UUID friendId);

    @Transactional
    void deleteByUserIdAndFriendId(UUID userId, UUID friendId);

}
