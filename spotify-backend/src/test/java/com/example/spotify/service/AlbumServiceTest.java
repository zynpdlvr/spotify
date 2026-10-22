
package com.example.spotify.service;

import com.example.spotify.dto.AlbumDTO;
import com.example.spotify.entity.Album;
import com.example.spotify.entity.AppUser;
import com.example.spotify.mapper.AlbumMapper;
import com.example.spotify.repository.AlbumRepository;
import com.example.spotify.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albums;

    @Mock
    private AlbumMapper albumMapper;

    @Mock
    private UserRepository users;

    @InjectMocks
    private AlbumService albumService;

    @Test
    void getExistingAlbumReturnsResponse() {
        // Arrange: Test verisini ve mock davranışlarını hazırla
        UUID publicId = UUID.randomUUID();

        Album album = new Album(
                "Test Album",
                "Test Description",
                "Test Artist"
        );

        AlbumDTO.AlbumResponse expected =
                mock(AlbumDTO.AlbumResponse.class);

        when(albums.findByPublicId(publicId))
                .thenReturn(Optional.of(album));

        when(albumMapper.toResponse(album))
                .thenReturn(expected);

        // Act: Test edilen metodu çalıştır
        AlbumDTO.AlbumResponse result = albumService.get(publicId);

        // Assert: Sonuç ve çağrılar doğru mu?
        assertSame(expected, result);

        verify(albums).findByPublicId(publicId);
        verify(albumMapper).toResponse(album);
    }

    @Test
    void getUnknownAlbumThrowsNotFound() {
        UUID unknownPublicId = UUID.randomUUID();

        when(albums.findByPublicId(unknownPublicId))
                .thenReturn(Optional.empty());

        var exception = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> albumService.get(unknownPublicId)
        );

        assertEquals(404, exception.getStatusCode().value());

        verify(albumMapper, never()).toResponse(any(Album.class));
    }

    @Test
    void artistCanCreateAlbumAndBecomesOwner() {
        UUID userPublicId = UUID.randomUUID();

        AppUser owner = mock(AppUser.class);

        AlbumDTO.AlbumRequest request = new AlbumDTO.AlbumRequest(
                "  Test Album  ",
                "Test Description",
                "  Test Artist  "
        );

        when(users.findByPublicId(userPublicId))
                .thenReturn(Optional.of(owner));

        when(albums.save(any(Album.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AlbumDTO.AlbumResponse expected =
                mock(AlbumDTO.AlbumResponse.class);

        when(albumMapper.toResponse(any(Album.class)))
                .thenReturn(expected);

        AlbumDTO.AlbumResponse result =
                albumService.create(request, userPublicId, false);

        assertSame(expected, result);

        org.mockito.ArgumentCaptor<Album> captor =
                org.mockito.ArgumentCaptor.forClass(Album.class);

        verify(albums).save(captor.capture());

        Album savedAlbum = captor.getValue();

        assertEquals("Test Album", savedAlbum.getName());
        assertEquals("Test Artist", savedAlbum.getArtistName());
        assertSame(owner, savedAlbum.getOwner());

        verify(albumMapper).toResponse(savedAlbum);
    }


}
