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

    @Column(nullable = false)
    private String postTitle;

    @Column
    private String content;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column
    private Boolean wishlist;

    @Column
    private Boolean deleted; // 삭제 여부 판별 필드

    @Column(name = "view_count", columnDefinition = "integer default 0", nullable = false)
    private int viewCount;

    @OneToOne
    @JoinColumn(name = "location_id")
    private Location location;

    //post에 연결된 song 리스트
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<PostSongLink> postSongLink = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Comment> Comment = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Wishlist> WishList = new ArrayList<>();

    // user와의 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 먼저 생성된 post에 song 추가
     * @param song
     */
    public PostSongLink addPostSongLink (Song song) {
        return PostSongLink.builder()
                .song(song)
                .post(this)
                .build();
    }


    /**
     * update메서드
     * @param postSaveRequestDto
     * @param newCategory
     */
    public void update(PostSaveRequestDto postSaveRequestDto , Category newCategory) {
        this.postTitle = postSaveRequestDto.getPostTitle();
        this.location.updateLocation(
                postSaveRequestDto.getLatitude(),
                postSaveRequestDto.getLongitude(),
                postSaveRequestDto.getAddress(),
                postSaveRequestDto.getPlaceName());
        this.category = newCategory;
        this.content = postSaveRequestDto.getContent();
    }

    // 삭제 값 변경 -> true로
    public void setDeleted(boolean deletedPost) {
        this.deleted = deletedPost;
    }

    public void viewCountUpdate() {
        this.viewCount++;

    }
}
