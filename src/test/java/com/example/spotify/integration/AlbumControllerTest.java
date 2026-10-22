package com.example.spotify.integration;

import com.example.spotify.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AlbumControllerTest extends IntegrationTestSupport {

    @Test
    void listAlbumsIsPublicAndHasSelfLink() throws Exception {
        createAlbum();

        mockMvc.perform(get("/api/v1/albums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void getAlbumReturnsPublicIdAndSongs() throws Exception {
        Album album = createAlbum();
        AppUser artist = createUser("album-song-owner@example.com", Role.ARTIST);

        createSong(artist, album, "Album Track");

        mockMvc.perform(get("/api/v1/albums/" + album.getPublicId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId")
                        .value(album.getPublicId().toString()))
                .andExpect(jsonPath("$.name").value("Test Album"))
                .andExpect(jsonPath("$.songs[0].title").value("Album Track"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void unknownAlbumReturnsNotFound() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/albums/bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb"))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCanCreateAlbumAndLocationHeaderIsSet() throws Exception {
        AppUser admin = createUser("album-admin@example.com", Role.ADMIN);

        mockMvc.perform(post("/api/v1/albums")
                        .header("Authorization", bearer(admin, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "New Album",
                              "description": "Description",
                              "artistName": "Artist"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("New Album"))
                .andExpect(jsonPath("$.publicId").isNotEmpty());
    }

    @Test
    void regularUserCannotCreateAlbum() throws Exception {
        AppUser user = createUser("album-user@example.com", Role.USER);

        mockMvc.perform(post("/api/v1/albums")
                        .header("Authorization", bearer(user, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Denied Album",
                              "artistName": "Artist"
                            }
                            """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createAlbumValidatesRequiredFields() throws Exception {
        AppUser admin = createUser("album-invalid-admin@example.com", Role.ADMIN);

        mockMvc.perform(post("/api/v1/albums")
                        .header("Authorization", bearer(admin, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "",
                              "artistName": ""
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCanPatchAlbum() throws Exception {
        AppUser admin = createUser("album-patch-admin@example.com", Role.ADMIN);
        Album album = createAlbum();

        mockMvc.perform(patch("/api/v1/albums/" + album.getPublicId())
                        .header("Authorization", bearer(admin, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name": "Renamed Album"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed Album"))
                .andExpect(jsonPath("$.artistName").value("Test Artist"));
    }

    @Test
    void deletingAlbumWithSongsReturnsConflict() throws Exception {
        AppUser admin = createUser("album-delete-admin@example.com", Role.ADMIN);
        AppUser artist = createUser("album-delete-artist@example.com", Role.ARTIST);

        Album album = createAlbum();
        createSong(artist, album, "Album Cannot Delete");

        mockMvc.perform(delete("/api/v1/albums/" + album.getPublicId())
                        .header("Authorization", bearer(admin, "ADMIN")))
                .andExpect(status().isConflict());
    }

    @Test
    void adminCanDeleteEmptyAlbum() throws Exception {
        AppUser admin = createUser("album-empty-delete-admin@example.com", Role.ADMIN);
        Album album = createAlbum();

        mockMvc.perform(delete("/api/v1/albums/" + album.getPublicId())
                        .header("Authorization", bearer(admin, "ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void albumWriteRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/albums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "No Token",
                              "artistName": "Artist"
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }
}