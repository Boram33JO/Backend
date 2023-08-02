package com.sparta.i_mu.entity;

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

    @Column
    private String title;

    @Column
    private String artist;

    @Column
    private String album;

    @Column
    private String thumbnailImage;

}
