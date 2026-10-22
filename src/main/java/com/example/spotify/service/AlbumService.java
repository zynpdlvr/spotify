
package com.example.spotify.service;

import com.example.spotify.dto.AlbumDTO;
import com.example.spotify.entity.Album;
import com.example.spotify.entity.AppUser;
import com.example.spotify.mapper.AlbumMapper;
import com.example.spotify.repository.AlbumRepository;

import com.example.spotify.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@Service
public class AlbumService {

    private final AlbumRepository albums;
    private final AlbumMapper albumMapper;
    private final UserRepository users;

    public AlbumService(
            AlbumRepository albums,
            AlbumMapper albumMapper,
            UserRepository users
    ) {
        this.albums = albums;
        this.albumMapper = albumMapper;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<AlbumDTO.AlbumResponse> all() {
        return albums.findAll()
                .stream()
                .map(albumMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlbumDTO.AlbumResponse get(UUID publicId) {
        return albumMapper.toResponse(find(publicId));
    }

    @Transactional
    public AlbumDTO.AlbumResponse create(
            AlbumDTO.AlbumRequest request,
            UUID userPublicId,
            boolean admin
    ) {
        Album album = new Album(
                request.name().trim(),
                request.description(),
                request.artistName().trim()
        );

        AppUser owner = users.findByPublicId(userPublicId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Kullanıcı bulunamadı."
                        )
                );

        // ARTIST kendi albümünün sahibi olur.
        // ADMIN albümü sahipsiz oluşturabilir.
        if (!admin) {
            album.setOwner(owner);
        }

        return albumMapper.toResponse(albums.save(album));
    }

    @Transactional
    public AlbumDTO.AlbumResponse update(
            UUID publicId,
            AlbumDTO.UpdateAlbumRequest request,
            UUID userPublicId,
            boolean admin
    ) {
        Album album = find(publicId);
        checkOwnership(album, userPublicId, admin);

        album.update(
                request.name() != null
                        ? request.name().trim()
                        : album.getName(),
                request.description() != null
                        ? request.description()
                        : album.getDescription(),
                request.artistName() != null
                        ? request.artistName().trim()
                        : album.getArtistName()
        );

        return albumMapper.toResponse(album);
    }

    @Transactional
    public void delete(UUID publicId, UUID userPublicId, boolean admin) {
        Album album = find(publicId);
        checkOwnership(album, userPublicId, admin);

        if (!album.getSongs().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Bu albümde şarkılar bulunduğu için albüm silinemez."
            );
        }

        albums.delete(album);
    }

    private Album find(UUID publicId) {
        return albums.findByPublicId(publicId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Albüm bulunamadı."
                        )
                );
    }

    private void checkOwnership(
            Album album,
            UUID userPublicId,
            boolean admin
    ) {
        if (admin) {
            return;
        }

        if (album.getOwner() == null
                || !album.getOwner().getPublicId().equals(userPublicId)) {
            throw new AccessDeniedException(
                    "Yalnızca kendi albümünüz üzerinde işlem yapabilirsiniz."
            );
        }
    }
}
