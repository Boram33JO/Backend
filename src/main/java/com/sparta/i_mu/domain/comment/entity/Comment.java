package com.sparta.i_mu.domain.comment.entity;

import com.sparta.i_mu.domain.post.entity.Post;
import com.sparta.i_mu.domain.user.entity.User;
import com.sparta.i_mu.domain.comment.dto.CommentRequestDto;
import com.sparta.i_mu.global.util.Timestamped;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
public class Comment extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column
    private Boolean deleted;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

//    @OneToMany(mappedBy = "comment", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
//    private Set<Notification> notifications = new HashSet<>();

    public void update(CommentRequestDto requestDto) {
        this.content = requestDto.getContent();
    }

    public void setDeleted(boolean deletedComment) {
        this.deleted = deletedComment;
    }

}
