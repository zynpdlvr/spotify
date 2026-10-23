
package com.example.spotify.service;

import com.example.spotify.dto.SongDTO;
import com.example.spotify.entity.Album;
import com.example.spotify.entity.AppUser;
import com.example.spotify.entity.Song;
import com.example.spotify.mapper.SongMapper;
import com.example.spotify.repository.AlbumRepository;
import com.example.spotify.repository.FavoriteRepository;
import com.example.spotify.repository.PlaylistRepository;
import com.example.spotify.repository.SongRepository;
import com.example.spotify.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@Service
public class SongService {

    private final SongRepository songs;
    private final PlaylistRepository playlists;
    private final FavoriteRepository favorites;
    private final AlbumRepository albumRepository;
    private final UserRepository users;
    private final SongMapper songMapper;

    public SongService(
            SongRepository songs,
            PlaylistRepository playlists,
            FavoriteRepository favorites,
            AlbumRepository albumRepository,
            UserRepository users,
            SongMapper songMapper
    ) {
        this.songs = songs;
        this.playlists = playlists;
        this.favorites = favorites;
        this.albumRepository = albumRepository;
        this.users = users;
        this.songMapper = songMapper;
    }

    // Şarkıları arar ve sayfalı sonuç döndürür.
    @Transactional(readOnly = true)
    public Page<SongDTO.SongResponse> all(
            String q,
            Pageable pageable
    ) {
        Page<Song> page;

        if (q == null || q.isBlank()) {
            page = songs.findAll(pageable);
        } else {
            String query = q.trim();

            page = songs
                    .findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
                            query,
                            query,
                            pageable
                    );
        }

        return page.map(songMapper::toResponse);
    }

    // UUID ile tek bir şarkıyı getirir.
    @Transactional(readOnly = true)
    public SongDTO.SongResponse get(UUID publicId) {
        return songMapper.toResponse(find(publicId));
    }

    // POST: Yeni şarkı oluşturur.
    @Transactional
    public SongDTO.SongResponse create(
            SongDTO.SongRequest req,
            JwtAuthenticationToken authentication
    ) {
        AppUser currentUser = getCurrentUser(authentication);
        UUID userPublicId = currentUser.getPublicId();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Album album = findAlbumForSong(
                req.albumPublicId(),
                currentUser.getPublicId(),
                isAdmin(authentication)
        );

        Song song = songMapper.toEntity(req);

        song.setTitle(req.title().trim());
        song.setArtist(req.artist().trim());
        song.setAlbum(album);
        song.setOwner(currentUser);

        return songMapper.toResponse(songs.save(song));
    }

    // PUT: Şarkının kaynak temsilini günceller.
    @Transactional
    public SongDTO.SongResponse update(
            UUID publicId,
            SongDTO.SongRequest req,
            JwtAuthenticationToken authentication
    ) {
        Song song = find(publicId);
        AppUser currentUser = getCurrentUser(authentication);
        checkUpdatePermission(song, currentUser, authentication);


        Album album = findAlbumForSong(
                req.albumPublicId(),
                currentUser.getPublicId(),
                isAdmin(authentication)
        );

        // DTO -> mevcut Entity alanlarının eşlenmesi
        songMapper.updateEntity(req, song);

        // İlişki Service tarafından doğrulanıp atanır
        song.setAlbum(album);

        return songMapper.toResponse(song);
    }

    // PATCH: Yalnızca gönderilen alanları günceller.
    @Transactional
    public SongDTO.SongResponse updateSong(
            UUID publicId,
            SongDTO.UpdateSongRequest request,
            JwtAuthenticationToken authentication
    ) {
        Song song = find(publicId);
        AppUser currentUser = getCurrentUser(authentication);

        checkUpdatePermission(song, currentUser, authentication);

        // Yalnızca null olmayan alanları eşle
        songMapper.updatePartial(request, song);

        // Albüm yalnızca istekte verilmişse değiştirilir
        if (request.albumPublicId() != null) {
            boolean isAdmin = authentication.getAuthorities()
                    .stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            song.setAlbum(findAlbumForSong(
                    request.albumPublicId(),
                    currentUser.getPublicId(),
                    isAdmin
            ));
        }

        return songMapper.toResponse(song);
    }
    // DELETE: Şarkıyı ve ilişkili kayıtlarını siler.
    @Transactional
    public void delete(
            UUID publicId,
            JwtAuthenticationToken authentication
    ) {
        Song song = find(publicId);
        AppUser currentUser = getCurrentUser(authentication);

        checkUpdatePermission(song, currentUser, authentication);

        // İlişkilerde veritabanının dahili Long ID'si kullanılır.
        Long internalId = song.getId();

        playlists.findAllBySongs_Id(internalId)
                .forEach(playlist -> playlist.removeSong(song));

        favorites.findAllBySong_Id(internalId)
                .forEach(favorite -> {
                    favorite.detachFromRelations();
                    favorites.delete(favorite);
                });

        songs.delete(song);
    }

    // UUID üzerinden şarkı bulur.
    @Transactional(readOnly = true)
    public Song find(UUID publicId) {
        return songs.findByPublicId(publicId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Şarkı bulunamadı."
                        )
                );
    }

    // UUID üzerinden albüm bulur.
    private Album findAlbum(UUID albumPublicId) {
        if (albumPublicId == null) {
            return null;
        }

        return albumRepository.findByPublicId(albumPublicId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Albüm bulunamadı."
                        )
                );
    }



    private boolean isAdmin(JwtAuthenticationToken authentication) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ROLE_ADMIN")
                                || a.getAuthority().equals("ADMIN")
                );
    }


    private Album findAlbumForSong(
            UUID albumPublicId,
            UUID userPublicId,
            boolean isAdmin
    ) {
        // Albümsüz şarkı oluşturulmasına izin ver.
        if (albumPublicId == null) {
            return null;
        }

        // Önce albümün gerçekten var olduğunu doğrula.
        Album album = albumRepository.findByPublicId(albumPublicId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Albüm bulunamadı."
                        )
                );

        // Admin albüm sahipliğinden bağımsız işlem yapabilir.
        if (isAdmin) {
            return album;
        }

        // Albümün sahibi yoksa normal sanatçı kullanamaz.
        if (album.getOwner() == null
                || album.getOwner().getPublicId() == null
                || !album.getOwner().getPublicId().equals(userPublicId)) {
            throw new AccessDeniedException(
                    "Bu albüme şarkı ekleme yetkiniz yok."
            );
        }

        return album;
    }


    // JWT subject üzerinden mevcut kullanıcıyı bulur.
    private AppUser getCurrentUser(JwtAuthenticationToken auth) {
        Long userId;

        try {
            userId = Long.valueOf(auth.getName());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "JWT subject geçerli bir kullanıcı ID'si değil."
            );
        }

        return users.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Kullanıcı bulunamadı."
                        )
                );
    }

    // ADMIN tüm şarkılarda işlem yapabilir.
    // Diğer yetkili kullanıcılar yalnızca kendi şarkılarını değiştirebilir.
    private void checkUpdatePermission(
            Song song,
            AppUser currentUser,
            JwtAuthenticationToken auth
    ) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a ->
                        a.getAuthority().equals("ADMIN")
                                || a.getAuthority().equals("ROLE_ADMIN")
                );

        if (isAdmin) {
            return;
        }

        if (song.getOwner() == null
                || !song.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "Yalnızca kendi şarkılarınız üzerinde işlem yapabilirsiniz."
            );
        }
    }
}
