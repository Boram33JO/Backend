package com.sparta.i_mu.entity;

import com.sparta.i_mu.dto.requestDto.PostSaveRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
public class Post extends Timestamped{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column
    private String content;

    @Column
    private String category;

    @OneToOne
    @JoinColumn(name = "location_id")
    private Location location;

    //post에 연결된 song 리스트
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<PostSongLink> postSongLink = new ArrayList<>();

    // user와의 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    /**
     * 먼저 생성된 post에 location 추가
     * @param location
     */
    public void addLocation(Location location) {
        this.location = location;
    }

    /**
     * 먼저 생성된 post에 song 추가
     * @param song
     */
    public void addPostSongLink (Song song) {
        PostSongLink postSongLink = PostSongLink.builder()
                .song(song)
                .post(this)
                .build();
    }

    /**
     * update 메서드
     * @param postSaveRequestDto
     */
    public void update(PostSaveRequestDto postSaveRequestDto) {
        this.location = postSaveRequestDto.getLocation();
        this.content = postSaveRequestDto.getContent();
        this.category = postSaveRequestDto.getCategory();
    }

    /**
     * Post와 Song을 연결한 postSongLink 해지
     */
    public void removeSongs() {
        this.postSongLink.clear();
    }
}
