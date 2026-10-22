package com.example.spotify.integration;

import com.example.spotify.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PlaylistControllerTest extends IntegrationTestSupport {

    @Test
    void createPlaylistReturnsCreatedAndOwnerPublicId() throws Exception {
        AppUser user = createUser("playlist-create@example.com", Role.USER);

        mockMvc.perform(post("/api/v1/playlists")
                        .header("Authorization", bearer(user, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "My Playlist",
                              "description": "Made in test"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("My Playlist"))
                .andExpect(jsonPath("$.ownerPublicId")
                        .value(user.getPublicId().toString()))
                .andExpect(jsonPath("$.ownerId").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.my-playlists.href").exists());
    }

    @Test
    void listOnlyReturnsCurrentUsersPlaylists() throws Exception {
        AppUser user = createUser("playlist-list@example.com", Role.USER);
        AppUser other = createUser("playlist-list-other@example.com", Role.USER);

        createPlaylist(user, "My List");
        createPlaylist(other, "Not My List");

        mockMvc.perform(get("/api/v1/playlists")
                        .header("Authorization", bearer(user, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("My List")))
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.containsString("Not My List"))));
    }

    @Test
    void getPlaylistReturnsNotFoundForAnotherOwnersPlaylist() throws Exception {
        AppUser owner = createUser("playlist-owner@example.com", Role.USER);
        AppUser other = createUser("playlist-other@example.com", Role.USER);

        Playlist playlist = createPlaylist(owner, "Private Playlist");

        mockMvc.perform(get("/api/v1/playlists/" + playlist.getPublicId())
                        .header("Authorization", bearer(other, "USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void ownerCanGetAndUpdatePlaylist() throws Exception {
        AppUser owner = createUser("playlist-update@example.com", Role.USER);
        Playlist playlist = createPlaylist(owner, "Before Update");

        mockMvc.perform(get("/api/v1/playlists/" + playlist.getPublicId())
                        .header("Authorization", bearer(owner, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Before Update"));

        mockMvc.perform(put("/api/v1/playlists/" + playlist.getPublicId())
                        .header("Authorization", bearer(owner, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "After Update",
                              "description": "New description"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("After Update"));
    }

    @Test
    void ownerCanDeletePlaylist() throws Exception {
        AppUser owner = createUser("playlist-delete@example.com", Role.USER);
        Playlist playlist = createPlaylist(owner, "Delete Me");

        mockMvc.perform(delete("/api/v1/playlists/" + playlist.getPublicId())
                        .header("Authorization", bearer(owner, "USER")))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions
                .assertThat(
                        playlists.findWithSongsByPublicIdAndOwner_Id(
                                playlist.getPublicId(),
                                owner.getId()
                        )
                )
                .isEmpty();
    }

    @Test
    void playlistNameIsRequired() throws Exception {
        AppUser owner = createUser("playlist-invalid@example.com", Role.USER);

        mockMvc.perform(post("/api/v1/playlists")
                        .header("Authorization", bearer(owner, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "",
                              "description": "Invalid"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void playlistEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/playlists"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void ownerCanAddAndRemoveSong() throws Exception {
        AppUser owner = createUser("playlist-songs@example.com", Role.USER);

        Song song = createSong(
                createUser("playlist-song-artist@example.com", Role.ARTIST),
                createAlbum(),
                "Playlist Track"
        );

        Playlist playlist = createPlaylist(owner, "Songs Playlist");

        mockMvc.perform(put(
                        "/api/v1/playlists/" + playlist.getPublicId()
                                + "/songs/" + song.getPublicId())
                        .header("Authorization", bearer(owner, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.songs[0].publicId")
                        .value(song.getPublicId().toString()));

        mockMvc.perform(delete(
                        "/api/v1/playlists/" + playlist.getPublicId()
                                + "/songs/" + song.getPublicId())
                        .header("Authorization", bearer(owner, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.songs").isEmpty());
    }
}