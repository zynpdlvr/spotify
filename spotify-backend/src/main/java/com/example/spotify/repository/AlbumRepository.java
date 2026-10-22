
package com.example.spotify.repository;

import com.example.spotify.entity.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    Optional<Album> findByPublicId(UUID publicId);

}
