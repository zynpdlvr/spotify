 package com.example.spotify.repository;

import com.example.spotify.entity.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SongRepository extends JpaRepository<Song, Long> {

    // Şarkıyı dışarıya açık UUID üzerinden bulur.
    Optional<Song> findByPublicId(UUID publicId);

    List<Song> findAllByOwner_Id(Long ownerId);

    // Başlıkta veya sanatçı adında arama yapar.
    Page<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
            String title,
            String artist,
            Pageable pageable
    );
}