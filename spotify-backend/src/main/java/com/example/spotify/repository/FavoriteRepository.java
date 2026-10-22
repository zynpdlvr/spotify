package com.example.spotify.repository;

import com.example.spotify.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
    List<Favorite> findAllByUser_Id(Long userId);
   List<Favorite> findAllBySong_Id(Long songId);
    Optional<Favorite> findByUser_IdAndSong_PublicId(
            Long userId,
            UUID songPublicId
    );
}
