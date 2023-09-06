package com.sparta.i_mu.domain.notification.entity;

import com.sparta.i_mu.domain.user.entity.User;
import com.sparta.i_mu.global.util.Timestamped;
import com.sparta.i_mu.global.util.NotificationType;
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

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(nullable = false, name = "comment_id")
//    private Comment comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    private String content;

    private String url;

    private boolean isRead;

    @Builder
    public Notification(User receiver, NotificationType notificationType, String content, String url, boolean isRead) {
        this.receiver = receiver;
        this.notificationType = notificationType;
        this.content = content;
        this.url = url;
        this.isRead = isRead;
    }

    public void read() {
        this.isRead = true;
    }
}
