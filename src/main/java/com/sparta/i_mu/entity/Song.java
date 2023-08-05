package com.sparta.i_mu.entity;

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
@Table
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
public class Song {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @Column
    private String songId;
    @Column
    private String title;
    @Column
    private String artistName;
    @Column
    private String album;
    @Column
    private String thumbnail;
    @Column
    private String external_url;

    @OneToMany(mappedBy = "song", fetch = FetchType.LAZY)
    private List<PostSongLink> postSongLink = new ArrayList<>();

}
