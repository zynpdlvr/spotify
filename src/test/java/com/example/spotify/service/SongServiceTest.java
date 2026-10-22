
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SongServiceTest {

    @Mock
    private SongRepository songs;

    @Mock
    private PlaylistRepository playlists;

    @Mock
    private FavoriteRepository favorites;

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private UserRepository users;

    @Mock
    private SongMapper songMapper;

    @InjectMocks
    private SongService songService;

    private AppUser artist;
    private AppUser anotherArtist;
    private UUID artistPublicId;
    private UUID anotherArtistPublicId;

    private JwtAuthenticationToken artistAuth;

    @BeforeEach
    void setUp() {
        artistPublicId = UUID.randomUUID();
        anotherArtistPublicId = UUID.randomUUID();

        artist = mock(AppUser.class);

        anotherArtist = mock(AppUser.class);

        artistAuth = authentication(1L, "ARTIST");

    }


    @Test
    void artistCanCreateSongInOwnAlbum() {
        // Arrange: Mevcut kullanıcıyı hazırla
        when(users.findById(1L)).thenReturn(Optional.of(artist));

        when(artist.getPublicId()).thenReturn(artistPublicId);

        Album album = albumOwnedBy(artist);

        Song song = new Song(
                "Test Song",
                "Test Artist",
                null,
                "https://example.com/test.mp3"
        );

        SongDTO.SongRequest request = new SongDTO.SongRequest(
                "Test Song",
                "Test Artist",
                album.getPublicId(),
                "https://example.com/test.mp3"
        );

        SongDTO.SongResponse response = responseFor(song, album);

        when(albumRepository.findByPublicId(album.getPublicId()))
                .thenReturn(Optional.of(album));
        when(songMapper.toEntity(request)).thenReturn(song);
        when(songs.save(song)).thenReturn(song);
        when(songMapper.toResponse(song)).thenReturn(response);

        // Act: Şarkıyı oluştur
        SongDTO.SongResponse result =
                songService.create(request, artistAuth);

        // Assert: Sonucu doğrula
        assertEquals("Test Song", result.title());
        assertEquals(album.getPublicId(), result.albumPublicId());

        verify(songs).save(song);
        verify(songMapper).toEntity(request);
    }


    @Test
    void artistCannotCreateSongInAnotherArtistsAlbum() {
        when(users.findById(1L)).thenReturn(Optional.of(artist));
        when(artist.getPublicId()).thenReturn(artistPublicId);
        when(anotherArtist.getPublicId()).thenReturn(anotherArtistPublicId);
        Album otherAlbum = albumOwnedBy(anotherArtist);

        SongDTO.SongRequest request = new SongDTO.SongRequest(
                "Unauthorized Song",
                "Test Artist",
                otherAlbum.getPublicId(),
                "https://example.com/test.mp3"
        );

        when(albumRepository.findByPublicId(otherAlbum.getPublicId()))
                .thenReturn(Optional.of(otherAlbum));

        assertThrows(
                AccessDeniedException.class,
                () -> songService.create(request, artistAuth)
        );

        verify(songs, never()).save(any(Song.class));
    }

    @Test
    void artistCannotUpdateAnotherArtistsSong() {
        Song song = new Song(
                "Original Song",
                "Original Artist",
                null,
                "https://example.com/original.mp3"
        );
        song.setOwner(anotherArtist);

        UUID songPublicId = song.getPublicId();

        SongDTO.SongRequest request = new SongDTO.SongRequest(
                "Changed Song",
                "Changed Artist",
                null,
                "https://example.com/changed.mp3"
        );

        when(users.findById(1L)).thenReturn(Optional.of(artist));

        when(artist.getId()).thenReturn(1L);
        when(anotherArtist.getId()).thenReturn(2L);
        when(songs.findByPublicId(songPublicId))
                .thenReturn(Optional.of(song));

        assertThrows(
                AccessDeniedException.class,
                () -> songService.update(
                        songPublicId,
                        request,
                        artistAuth
                )
        );

        verify(songMapper, never())
                .updateEntity(any(), any(Song.class));
    }

    @Test
    void artistCannotMoveSongToAnotherArtistsAlbum() {
        Album ownAlbum = albumOwnedBy(artist);
        Album otherAlbum = albumOwnedBy(anotherArtist);

        Song song = new Song(
                "My Song",
                "My Artist",
                ownAlbum,
                "https://example.com/my-song.mp3"
        );
        song.setOwner(artist);

        when(users.findById(1L)).thenReturn(Optional.of(artist));

        when(artist.getId()).thenReturn(1L);

        when(artist.getPublicId()).thenReturn(artistPublicId);
        when(anotherArtist.getPublicId()).thenReturn(anotherArtistPublicId);
        UUID songPublicId = song.getPublicId();

        SongDTO.UpdateSongRequest request =
                new SongDTO.UpdateSongRequest(
                        null,
                        null,
                        otherAlbum.getPublicId(),
                        null
                );

        when(songs.findByPublicId(songPublicId))
                .thenReturn(Optional.of(song));
        when(albumRepository.findByPublicId(otherAlbum.getPublicId()))
                .thenReturn(Optional.of(otherAlbum));

        assertThrows(
                AccessDeniedException.class,
                () -> songService.updateSong(
                        songPublicId,
                        request,
                        artistAuth
                )
        );

        assertSame(ownAlbum, song.getAlbum());
    }

    @Test
    void createSongWithUnknownAlbumReturnsNotFound() {
        UUID unknownAlbumId = UUID.randomUUID();

        SongDTO.SongRequest request = new SongDTO.SongRequest(
                "Test Song",
                "Test Artist",
                unknownAlbumId,
                "https://example.com/test.mp3"
        );

        when(users.findById(1L)).thenReturn(Optional.of(artist));
        when(albumRepository.findByPublicId(unknownAlbumId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> songService.create(request, artistAuth)
        );

        assertEquals(404, exception.getStatusCode().value());
        verify(songs, never()).save(any(Song.class));
    }

    @Test
    void adminCanCreateSongInAnotherArtistsAlbum() {
        Album otherAlbum = albumOwnedBy(anotherArtist);

        JwtAuthenticationToken adminAuth =
                authentication(3L, "ADMIN");

        AppUser admin = mock(AppUser.class);


        when(admin.getPublicId()).thenReturn(UUID.randomUUID());
        when(users.findById(3L)).thenReturn(Optional.of(admin));

        Song song = new Song(
                "Admin Created Song",
                "Test Artist",
                null,
                "https://example.com/admin.mp3"
        );

        SongDTO.SongRequest request = new SongDTO.SongRequest(
                "Admin Created Song",
                "Test Artist",
                otherAlbum.getPublicId(),
                "https://example.com/admin.mp3"
        );

        when(albumRepository.findByPublicId(otherAlbum.getPublicId()))
                .thenReturn(Optional.of(otherAlbum));
        when(songMapper.toEntity(request)).thenReturn(song);
        when(songs.save(song)).thenReturn(song);
        when(songMapper.toResponse(song))
                .thenReturn(responseFor(song, otherAlbum));

        SongDTO.SongResponse result =
                songService.create(request, adminAuth);

        assertEquals(otherAlbum.getPublicId(), result.albumPublicId());
        verify(songs).save(song);
    }

    @Test
    void artistCanUpdateOwnSong() {
        when(users.findById(1L)).thenReturn(Optional.of(artist));
        when(artist.getId()).thenReturn(1L);
        when(artist.getPublicId()).thenReturn(artistPublicId);

        Album album = albumOwnedBy(artist);

        Song song = new Song(
                "Original Song",
                "Original Artist",
                album,
                "https://example.com/original.mp3"
        );
        song.setOwner(artist);

        UUID songPublicId = song.getPublicId();

        SongDTO.SongRequest request = new SongDTO.SongRequest(
                "Updated Song",
                "Updated Artist",
                album.getPublicId(),
                "https://example.com/updated.mp3"
        );

        when(songs.findByPublicId(songPublicId))
                .thenReturn(Optional.of(song));
        when(albumRepository.findByPublicId(album.getPublicId()))
                .thenReturn(Optional.of(album));

        // Mapper'ın alanları güncellemesini simüle ediyoruz.
        doAnswer(invocation -> {
            SongDTO.SongRequest req = invocation.getArgument(0);
            Song target = invocation.getArgument(1);

            target.setTitle(req.title());
            target.setArtist(req.artist());
            target.setAudioUrl(req.audioUrl());

            return null;
        }).when(songMapper).updateEntity(request, song);

        when(songMapper.toResponse(song))
                .thenAnswer(invocation -> responseFor(song, song.getAlbum()));

        SongDTO.SongResponse result =
                songService.update(songPublicId, request, artistAuth);

        assertEquals("Updated Song", result.title());
        assertEquals("Updated Artist", result.artist());
        assertEquals("https://example.com/updated.mp3", result.audioUrl());
        assertEquals(album.getPublicId(), result.albumPublicId());

        verify(songMapper).updateEntity(request, song);
    }

    @Test
    void artistCanPartiallyUpdateOwnSong() {
        when(users.findById(1L)).thenReturn(Optional.of(artist));
        when(artist.getId()).thenReturn(1L);

        Album album = albumOwnedBy(artist);

        Song song = new Song(
                "Original Song",
                "Original Artist",
                album,
                "https://example.com/original.mp3"
        );
        song.setOwner(artist);

        UUID songPublicId = song.getPublicId();

        SongDTO.UpdateSongRequest request =
                new SongDTO.UpdateSongRequest(
                        "PATCH Updated Song",
                        null,
                        null,
                        null
                );

        when(songs.findByPublicId(songPublicId))
                .thenReturn(Optional.of(song));

        doAnswer(invocation -> {
            SongDTO.UpdateSongRequest req = invocation.getArgument(0);
            Song target = invocation.getArgument(1);

            if (req.title() != null) {
                target.setTitle(req.title());
            }
            if (req.artist() != null) {
                target.setArtist(req.artist());
            }
            if (req.audioUrl() != null) {
                target.setAudioUrl(req.audioUrl());
            }

            return null;
        }).when(songMapper).updatePartial(request, song);

        when(songMapper.toResponse(song))
                .thenAnswer(invocation -> responseFor(song, song.getAlbum()));

        SongDTO.SongResponse result =
                songService.updateSong(songPublicId, request, artistAuth);

        assertEquals("PATCH Updated Song", result.title());
        assertEquals("Original Artist", result.artist());
        assertEquals("https://example.com/original.mp3", result.audioUrl());
        assertSame(album, song.getAlbum());

        verify(songMapper).updatePartial(request, song);
    }

    @Test
    void updatingUnknownSongReturnsNotFound() {
        UUID unknownSongId = UUID.randomUUID();

        when(songs.findByPublicId(unknownSongId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> songService.update(
                        unknownSongId,
                        new SongDTO.SongRequest(
                                "Updated Song",
                                "Updated Artist",
                                null,
                                "https://example.com/updated.mp3"
                        ),
                        artistAuth
                )
        );

        assertEquals(404, exception.getStatusCode().value());

        verify(users, never()).findById(anyLong());
        verify(songMapper, never()).updateEntity(any(), any(Song.class));
    }

    @Test
    void adminCanUpdateAnotherArtistsSong() {
        JwtAuthenticationToken adminAuth = authentication(3L, "ADMIN");
        AppUser admin = mock(AppUser.class);

        when(users.findById(3L)).thenReturn(Optional.of(admin));

        Song song = new Song(
                "Artist Song",
                "Original Artist",
                null,
                "https://example.com/original.mp3"
        );
        song.setOwner(anotherArtist);

        UUID songPublicId = song.getPublicId();

        SongDTO.UpdateSongRequest request =
                new SongDTO.UpdateSongRequest(
                        "Admin Updated Song",
                        null,
                        null,
                        null
                );

        when(songs.findByPublicId(songPublicId))
                .thenReturn(Optional.of(song));

        doAnswer(invocation -> {
            SongDTO.UpdateSongRequest req = invocation.getArgument(0);
            Song target = invocation.getArgument(1);

            if (req.title() != null) {
                target.setTitle(req.title());
            }

            return null;
        }).when(songMapper).updatePartial(request, song);

        when(songMapper.toResponse(song))
                .thenAnswer(invocation -> responseFor(song, song.getAlbum()));

        SongDTO.SongResponse result =
                songService.updateSong(songPublicId, request, adminAuth);

        assertEquals("Admin Updated Song", result.title());

        verify(songMapper).updatePartial(request, song);
    }


    private Album albumOwnedBy(AppUser owner) {
        Album album = new Album(
                "Test Album",
                "Test Description",
                "Test Artist"
        );

        album.setOwner(owner);
        return album;
    }

    private SongDTO.SongResponse responseFor(
            Song song,
            Album album
    ) {
        return new SongDTO.SongResponse(
                song.getPublicId(),
                song.getTitle(),
                song.getArtist(),
                album == null ? null : album.getPublicId(),
                album == null ? null : album.getName(),
                song.getAudioUrl(),
                song.getCreatedAt()
        );
    }

    private JwtAuthenticationToken authentication(
            Long userId,
            String role
    ) {
        Instant now = Instant.now();

        Jwt jwt = new Jwt(
                "test-token",
                now,
                now.plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", userId.toString())
        );

        return new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }
}
