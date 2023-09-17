package com.sparta.i_mu.domain.song.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @Column(nullable = false, unique = true)
    private String songNum;
    @Column(nullable = false)
    private String songTitle;
    @Column(nullable = false)
    private String artistName;
    @Column(nullable = false)
    private String album;
    @Column
    private String audioUrl;
    @Column
    private String thumbnail;
    @Column(nullable = false)
    private String externalUrl;

}
