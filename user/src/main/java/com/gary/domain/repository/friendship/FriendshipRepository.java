package com.gary.domain.repository.friendship;

import com.gary.domain.model.friendship.Friendship;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findByUserId(Long userId);

    boolean existsByUserIdAndFriendId(Long userId, Long friendId);

    @Transactional
    void deleteByUserIdAndFriendId(Long userId, Long friendId);

}
