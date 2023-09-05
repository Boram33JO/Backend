package com.sparta.i_mu.domain.follow.entity;

import com.sparta.i_mu.domain.user.entity.User;
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
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 내가 팔로우를 당한
    @ManyToOne
    @JoinColumn(name = "follow_user_id")
    private User followUser;

    // 내가 팔로우를 한
    @ManyToOne
    @JoinColumn(name = "follwed_user_id")
    private User followedUser;

}

