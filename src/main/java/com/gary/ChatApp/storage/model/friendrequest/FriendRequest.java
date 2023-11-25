package com.gary.ChatApp.storage.model.friendrequest;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.AUTO;


@Entity(name = "FriendRequest")
@Table(name = "chat_message")
@Data
@NoArgsConstructor
public class FriendRequest {

    @Id
    @SequenceGenerator(
            name = "friend_sequence",
            sequenceName = "friend_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = AUTO,
            generator = "friend_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
    private Long id;

    @Column(
            name = "senderId",
            updatable = false
    )
    private Long senderId;

    @Column(
            name = "receiverId",
            updatable = false
    )
    private Long receiverId;

    private RequestStatus requestStatus;

    public FriendRequest(Long senderId, Long receiverId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

}
