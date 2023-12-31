package com.sparta.i_mu.domain.post.entity;

import com.sparta.i_mu.domain.category.entity.Category;
import com.sparta.i_mu.domain.location.entity.Location;
import com.sparta.i_mu.domain.postsonglink.entity.PostSongLink;
import com.sparta.i_mu.domain.song.entity.Song;
import com.sparta.i_mu.domain.user.entity.User;
import com.sparta.i_mu.domain.wishlist.entity.Wishlist;
import com.sparta.i_mu.domain.post.dto.PostSaveRequestDto;
import com.sparta.i_mu.global.util.Timestamped;
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
public class Post extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(nullable = false)
    private String postTitle;

    @Column(length = 500)
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

    @Column(name = "wishlist_count", columnDefinition = "integer default 0", nullable = false)
    private int wishlistCount;

    @OneToOne
    @JoinColumn(name = "location_id")
    private Location location;

    //post에 연결된 song 리스트
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<PostSongLink> postSongLink = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<com.sparta.i_mu.domain.comment.entity.Comment> Comment = new ArrayList<>();

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

    // 조회수
    public void viewCountUpdate() {
        this.viewCount++;
    }
  
    // 
    public void downWishlistCount() {
        this.wishlistCount--;
    }

    public void upWishlistCount() {
        this.wishlistCount++;
    }

}
