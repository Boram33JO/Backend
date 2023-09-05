package com.sparta.i_mu.domain.location.repository;

import com.sparta.i_mu.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    @Query("SELECT l.placeName " +
            "FROM Location l LEFT JOIN Post p ON l.id = p.location.id " +  // Location과 Post 사이의 LEFT JOIN
            "WHERE p.deleted = false " +
            "GROUP BY l.placeName " +
            "ORDER BY COUNT(l.placeName) DESC")
    List<String> findAllByTopLocations();
}
