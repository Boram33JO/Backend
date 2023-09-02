package com.sparta.i_mu.entity;

import com.nimbusds.oauth2.sdk.Role;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
public class User {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String phonenumber;

    @Enumerated(STRING)
    private Role role;

    @Column
    private String introduce;

    @Column
    private String userImage;

    @Column(nullable = false, unique = true)

    private Long kakaoId;


    public void update(User user) {
        this.nickname = user.getNickname();
        this.introduce = user.getIntroduce();
        this.userImage = user.getUserImage();
    }

    public void passwordUpdate(User user) {
        this.password = user.getPassword();
    }

    public User kakaoIdUpdate(Long kakaoId) {
        this.kakaoId = kakaoId;
        return this;
    }
}
