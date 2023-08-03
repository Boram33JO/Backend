package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    Long countByPostId(Long id);
}
