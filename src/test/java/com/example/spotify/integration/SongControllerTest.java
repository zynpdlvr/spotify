package com.example.spotify.integration;

import com.example.spotify.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SongControllerTest extends IntegrationTestSupport {

    @Test
    void listSongsIsPublicAndContainsSelfLink() throws Exception {
        AppUser owner = createUser("song-list-owner@example.com", Role.ARTIST);
        createSong(owner, createAlbum(), "Public Test Song");

        mockMvc.perform(get("/api/v1/songs")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void searchSongsFiltersByTitle() throws Exception {
        AppUser owner = createUser("song-search-owner@example.com", Role.ARTIST);

        createSong(owner, createAlbum(), "Needle Search Song");
        createSong(owner, createAlbum(), "Completely Different");

        mockMvc.perform(get("/api/v1/songs")
                        .param("q", "Needle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists());
    }

    @Test
    void getSongReturnsDetailsAndHateoasLinks() throws Exception {
        AppUser owner = createUser("song-get-owner@example.com", Role.ARTIST);
        Song song = createSong(owner, createAlbum(), "Get Me Song");

        mockMvc.perform(get("/api/v1/songs/" + song.getPublicId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId")
                        .value(song.getPublicId().toString()))
                .andExpect(jsonPath("$.title").value("Get Me Song"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.songs.href").exists());
    }

    @Test
    void getUnknownSongReturnsNotFound() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/songs/aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa"))
                .andExpect(status().isNotFound());
    }

    @Test
    void artistCanCreateSongAndResponseUsesPublicIds() throws Exception {
        AppUser artist = createUser("song-create-artist@example.com", Role.ARTIST);
        Album album = createAlbum();

        mockMvc.perform(post("/api/v1/songs")
                        .header("Authorization", bearer(artist, "ARTIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Created Through API",
                              "artist": "Test Artist",
                              "albumPublicId": "%s",
                              "audioUrl": "https://example.com/new.mp3"
                            }
                            """.formatted(album.getPublicId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").isNotEmpty())
                .andExpect(jsonPath("$.albumPublicId")
                        .value(album.getPublicId().toString()))
                .andExpect(jsonPath("$.albumName").value("Test Album"))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void regularUserCannotCreateSong() throws Exception {
        AppUser user = createUser("song-create-user@example.com", Role.USER);

        mockMvc.perform(post("/api/v1/songs")
                        .header("Authorization", bearer(user, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "Denied Song",
                              "artist": "Test Artist",
                              "audioUrl": "https://example.com/a.mp3"
                            }
                            """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createSongValidatesRequiredFields() throws Exception {
        AppUser artist = createUser("song-invalid-artist@example.com", Role.ARTIST);

        mockMvc.perform(post("/api/v1/songs")
                        .header("Authorization", bearer(artist, "ARTIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "",
                              "artist": "Test Artist"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchChangesOnlySuppliedFields() throws Exception {
        AppUser artist = createUser("song-patch-owner@example.com", Role.ARTIST);
        Song song = createSong(artist, createAlbum(), "Before Patch");

        mockMvc.perform(patch("/api/v1/songs/" + song.getPublicId())
                        .header("Authorization", bearer(artist, "ARTIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"title": "After Patch"}
                            """))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("After Patch"))
                .andExpect(jsonPath("$.artist").value("Test Artist"));
    }

    @Test
    void putReplacesSongFields() throws Exception {
        AppUser artist = createUser("song-put-owner@example.com", Role.ARTIST);
        Song song = createSong(artist, createAlbum(), "Before Put");

        mockMvc.perform(put("/api/v1/songs/" + song.getPublicId())
                        .header("Authorization", bearer(artist, "ARTIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "After Put",
                              "artist": "Updated Artist",
                              "audioUrl": "https://example.com/put.mp3"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("After Put"))
                .andExpect(jsonPath("$.artist").value("Updated Artist"));
    }

    @Test
    void artistCannotModifyAnotherArtistsSong() throws Exception {
        AppUser owner = createUser("song-owner@example.com", Role.ARTIST);
        AppUser other = createUser("song-other@example.com", Role.ARTIST);
        Song song = createSong(owner, createAlbum(), "Protected Song");

        mockMvc.perform(patch("/api/v1/songs/" + song.getPublicId())
                        .header("Authorization", bearer(other, "ARTIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"title": "Attempted Takeover"}
                            """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanDeleteSong() throws Exception {
        AppUser owner = createUser("song-delete-owner@example.com", Role.ARTIST);
        AppUser admin = createUser("song-delete-admin@example.com", Role.ADMIN);
        Song song = createSong(owner, createAlbum(), "Delete This Song");

        mockMvc.perform(delete("/api/v1/songs/" + song.getPublicId())
                        .header("Authorization", bearer(admin, "ADMIN")))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions
                .assertThat(songs.findByPublicId(song.getPublicId()))
                .isEmpty();
    }

    @Test
    void writeOperationsRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/songs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title": "No Token",
                              "artist": "Test Artist"
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }
}