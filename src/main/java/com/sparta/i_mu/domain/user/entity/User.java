package com.sparta.i_mu.domain.user.entity;

import com.sparta.i_mu.global.util.Timestamped;
import jakarta.persistence.*;
import lombok.*;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
public class User extends Timestamped {

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

    @Column(unique = true)
    private String phoneNumber;

    @Column
    private String introduce;

    @Column
    private String userImage;

    @Column
    private Long kakaoId;

    @Column
    @Builder.Default
    private Boolean deleted = false; // 삭제 여부 판별 필드

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

    public void setDeleted(boolean deletedUser) {
        this.deleted = deletedUser;
    }

    public void setPassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void setNickname() {
        this.nickname = null;
    }
}
