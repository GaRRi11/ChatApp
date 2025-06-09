package com.gary.domain.repository;

import com.gary.domain.model.friendship.Friendship;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findByUserId(Long userId);

    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);

    @Transactional
    void deleteByUserIdAndFriendId(Long userId, Long friendId);

}
