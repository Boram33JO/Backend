package com.sparta.i_mu.domain.notification.entity;

import com.sparta.i_mu.domain.user.entity.User;
import com.sparta.i_mu.global.util.Timestamped;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@NoArgsConstructor
public class Notification extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "receiver_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "sender_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User sender;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(nullable = false, name = "comment_id")
//    private Comment comment;


    private String content;

    private Long postId;

    private String postTitle;

    private boolean isRead;

    @Builder
    public Notification(User receiver, User sender, String content, Long postId, String postTitle, boolean isRead) {
        this.receiver = receiver;
        this.sender = sender;
        this.content = content;
        this.postId = postId;
        this.postTitle = postTitle;
        this.isRead = isRead;
    }

    public void read() {
        this.isRead = true;
    }
}
