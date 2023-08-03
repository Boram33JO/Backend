package com.sparta.i_mu.repository;

import com.sparta.i_mu.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.category = :category AND ST_Distance_Sphere(Point(p.location.longitude, p.location.latitude), Point(:longitude, :latitude)) <= :DISTANCE_IN_METERS")
    List<Post> findAllByCategoryAndLocationNear(String category, Double latitude, Double longitude, Double DISTANCE_IN_METERS);
    @Query("SELECT p FROM Post p WHERE ST_Distance_Sphere(Point(p.location.longitude, p.location.latitude), Point(:longitude, :latitude)) <= :DISTANCE_IN_METERS")
    List<Post> findAllByLocationNear(Double latitude, Double longitude, Double DISTANCE_IN_METERS);
}
