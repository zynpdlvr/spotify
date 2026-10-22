package com.example.spotify.integration;

import com.example.spotify.entity.*;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FavoriteControllerTest extends IntegrationTestSupport {

    @Test
    void favoritesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/me/favorites"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addListAndRemoveFavorite() throws Exception {
        AppUser user = createUser("favorites-user@example.com", Role.USER);
        AppUser artist = createUser("favorites-artist@example.com", Role.ARTIST);

        Song song = createSong(
                artist,
                createAlbum(),
                "Favorite Track"
        );

        String auth = bearer(user, "USER");

        mockMvc.perform(put(
                        "/api/v1/me/favorites/" + song.getPublicId())
                        .header("Authorization", auth))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/me/favorites")
                        .header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].publicId")
                        .value(song.getPublicId().toString()))
                .andExpect(jsonPath("$[0].title").value("Favorite Track"));

        mockMvc.perform(delete(
                        "/api/v1/me/favorites/" + song.getPublicId())
                        .header("Authorization", auth))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/me/favorites")
                        .header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void addingSameFavoriteTwiceDoesNotCreateDuplicate() throws Exception {
        AppUser user = createUser(
                "favorites-duplicate-user@example.com",
                Role.USER
        );

        Song song = createSong(
                createUser("favorites-duplicate-artist@example.com", Role.ARTIST),
                createAlbum(),
                "Unique Favorite"
        );

        String auth = bearer(user, "USER");

        mockMvc.perform(put(
                        "/api/v1/me/favorites/" + song.getPublicId())
                        .header("Authorization", auth))
                .andExpect(status().isNoContent());

        mockMvc.perform(put(
                        "/api/v1/me/favorites/" + song.getPublicId())
                        .header("Authorization", auth))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/me/favorites")
                        .header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void removingNonexistentFavoriteReturnsNotFound() throws Exception {
        AppUser user = createUser(
                "favorites-missing-user@example.com",
                Role.USER
        );

        mockMvc.perform(delete(
                        "/api/v1/me/favorites/cccccccc-cccc-4ccc-8ccc-cccccccccccc")
                        .header("Authorization", bearer(user, "USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void addingUnknownSongReturnsNotFound() throws Exception {
        AppUser user = createUser(
                "favorites-unknown-song-user@example.com",
                Role.USER
        );

        mockMvc.perform(put(
                        "/api/v1/me/favorites/dddddddd-dddd-4ddd-8ddd-dddddddddddd")
                        .header("Authorization", bearer(user, "USER")))
                .andExpect(status().isNotFound());
    }
}