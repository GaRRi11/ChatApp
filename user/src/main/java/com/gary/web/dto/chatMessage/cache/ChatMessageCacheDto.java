package com.gary.web.dto.chatMessage.cache;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Document;
import jakarta.persistence.Id;
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
    private long ttl = 21600;
}
