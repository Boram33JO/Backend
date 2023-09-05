package com.sparta.i_mu.domain.postsonglink.entity;

import com.sparta.i_mu.domain.post.entity.Post;
import com.sparta.i_mu.domain.song.entity.Song;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostSongLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "song_id")
    private Song song;
}
