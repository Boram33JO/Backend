package com.sparta.i_mu.domain.song.repository.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.i_mu.domain.post.entity.QPost;
import com.sparta.i_mu.domain.postsonglink.entity.QPostSongLink;
import com.sparta.i_mu.domain.song.entity.QSong;
import com.sparta.i_mu.domain.song.entity.Song;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class CustomSongRepositoryImpl implements CustomSongRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Song> findByCategoryIdOrderByPostCountDesc(Long categoryId) {
        QPost qPost = QPost.post;
        QPostSongLink qPostSongLink = QPostSongLink.postSongLink;

        return jpaQueryFactory
                .select(qPostSongLink.song)
                .from(qPostSongLink)
                .join(qPostSongLink.post, qPost)
                .where(qPost.category.id.eq(categoryId))
                .groupBy(qPostSongLink.song)
                .orderBy(qPostSongLink.song.count().desc())
                .limit(4)
                .fetch();
    }

}
