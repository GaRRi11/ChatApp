package com.gary.web.dto.cache.chatMessage;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Document;
import org.springframework.data.annotation.Id; // ✅ Correct one
import lombok.*;
import org.springframework.data.redis.core.TimeToLive;

import java.time.LocalDateTime;
import java.util.UUID;

@Document
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ChatMessageCacheDto {

    @Id
    @Indexed
    private UUID id;

    @Indexed
    private UUID senderId;

    @Indexed
    private UUID receiverId;

    private String content;

    private LocalDateTime timestamp;

    @TimeToLive
    @Builder.Default
    private long ttl = 21600; // 6 hours
}
