package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.Post;
import com.sparta.i_mu.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    @Query("SELECT w.post, COUNT(w) FROM Wishlist w GROUP BY w.post ORDER BY COUNT(w) DESC")
    List<Post> findPostsByWishlistCountDesc();

    Long countByPostId(Long id);

    Optional<Wishlist> findByPostIdAndUserId(Long postId, Long userId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    List<Wishlist> findAllByUserIdAndPostDeletedFalse(Long userId);
}
