package com.example.spotify.integration;

import com.example.spotify.entity.*;
import com.example.spotify.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtEncoder jwtEncoder;

    @Autowired
    protected UserRepository users;

    @Autowired
    protected AlbumRepository albums;

    @Autowired
    protected SongRepository songs;

    @Autowired
    protected PlaylistRepository playlists;

    @Autowired
    protected FavoriteRepository favorites;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected AppUser createUser(String email, Role role) {
        AppUser user = new AppUser(
                "Test User",
                email,
                passwordEncoder.encode("Password123!")
        );

        user.setRole(role);

        return users.saveAndFlush(user);
    }
    protected Album createAlbum() {
        return albums.saveAndFlush(
                new Album(
                        "Test Album",
                        "Album description",
                        "Test Artist"
                )
        );
    }

    protected Album createAlbum(AppUser owner) {
        Album album = new Album(
                "Test Album",
                "Album description",
                "Test Artist"
        );

        album.setOwner(owner);

        return albums.saveAndFlush(album);
    }

    protected Song createSong(AppUser owner, Album album, String title) {
        Song song = new Song(
                title,
                "Test Artist",
                album,
                "https://example.com/song.mp3"
        );

        song.setOwner(owner);

        return songs.saveAndFlush(song);
    }

    protected Playlist createPlaylist(AppUser owner, String name) {
        return playlists.saveAndFlush(
                new Playlist(
                        name,
                        "Playlist description",
                        owner
                )
        );
    }

    protected String token(AppUser user, String... authorities) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("spotify-backend")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(600))
                .subject(user.getId().toString())
                .claim("publicId", user.getPublicId().toString())
                .claim("email", user.getEmail())
                .claim("authorities", List.of(authorities))
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(),
                        claims
                )
        ).getTokenValue();
    }

    protected String bearer(AppUser user, String... authorities) {
        return "Bearer " + token(user, authorities);
    }

    protected String json(String value) {
        return value;
    }
}