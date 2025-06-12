package com.gary.application.friendship;


import com.gary.domain.event.friendshipRemoved.FriendshipRemovedEvent;
import com.gary.domain.service.cache.ChatCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendshipRemovedEventListener {

    private final ChatCacheService chatCacheService;

    @EventListener
    public void onFriendshipRemoved(FriendshipRemovedEvent event) {
        chatCacheService.clearCachedMessages(event.getUserId(), event.getFriendId());
        log.info("Evicted chat cache after friendship removed: userId={}, friendId={}",
                event.getUserId(), event.getFriendId());
    }
}
