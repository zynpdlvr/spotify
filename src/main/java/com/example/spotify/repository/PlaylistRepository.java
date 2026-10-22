package com.example.spotify.repository;

import com.example.spotify.entity.Playlist;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findAllByOwner_IdOrderByCreatedAtDesc(Long ownerId);

    Optional<Playlist> findByIdAndOwner_Id(Long id, Long ownerId);

    List<Playlist> findAllBySongs_Id(Long songId);

    List<Playlist> findAllByOwner_Id(Long ownerId);
    Optional<Playlist> findWithSongsByPublicIdAndOwner_Id(
            UUID publicId,
            Long ownerId
    );
}
