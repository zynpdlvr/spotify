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
        createSong(owner, createAlbum(owner), "Public Test Song");

        mockMvc.perform(get("/api/v1/songs")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void searchSongsFiltersByTitle() throws Exception {
        AppUser owner = createUser("song-search-owner@example.com", Role.ARTIST);

        createSong(owner, createAlbum(owner), "Needle Search Song");
        createSong(owner, createAlbum(), "Completely Different");

        mockMvc.perform(get("/api/v1/songs")
                        .param("q", "Needle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists());
    }

    @Test
    void getSongReturnsDetailsAndHateoasLinks() throws Exception {
        AppUser owner = createUser("song-get-owner@example.com", Role.ARTIST);
        Song song = createSong(owner, createAlbum(owner), "Get Me Song");

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
        AppUser artist = createUser(
                "song-create-artist@example.com",
                Role.ARTIST
        );

        Album album = createAlbum(artist);

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
        Song song = createSong(artist, createAlbum(artist), "Before Patch");

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
        Song song = createSong(artist, createAlbum(artist), "Before Put");

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
        Song song = createSong(owner, createAlbum(owner), "Protected Song");

        mockMvc.perform(patch("/api/v1/songs/" + song.getPublicId())
                        .header("Authorization", bearer(other, "ARTIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"title": "Attempted Takeover"}
                            """))
                .andExpect(status().isForbidden());
    }


    @Test
    void artistCannotCreateSongInAnotherArtistsAlbum() throws Exception {
        AppUser artist = createUser(
                "song-cross-album-artist@example.com", Role.ARTIST);
        AppUser albumOwner = createUser(
                "song-cross-album-owner@example.com", Role.ARTIST);

        Album otherArtistsAlbum = createAlbum(albumOwner);

        mockMvc.perform(post("/api/v1/songs")
                        .header("Authorization", bearer(artist, "ARTIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "title": "Unauthorized Song",
                          "artist": "Test Artist",
                          "albumPublicId": "%s",
                          "audioUrl": "https://example.com/unauthorized.mp3"
                        }
                        """.formatted(otherArtistsAlbum.getPublicId())))
                .andExpect(status().isForbidden());
    }


    @Test
    void artistCannotMoveSongToAnotherArtistsAlbum() throws Exception {
        AppUser artist = createUser(
                "song-move-artist@example.com", Role.ARTIST);
        AppUser otherArtist = createUser(
                "song-move-other@example.com", Role.ARTIST);

        Album ownAlbum = createAlbum(artist);
        Album otherAlbum = createAlbum(otherArtist);

        Song song = createSong(artist, ownAlbum, "My Original Song");

        mockMvc.perform(patch("/api/v1/songs/" + song.getPublicId())
                        .header("Authorization", bearer(artist, "ARTIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "albumPublicId": "%s"
                        }
                        """.formatted(otherAlbum.getPublicId())))
                .andExpect(status().isForbidden());
    }


    @Test
    void artistCannotDeleteAnotherArtistsSong() throws Exception {
        AppUser owner = createUser(
                "song-delete-owner@example.com", Role.ARTIST);
        AppUser otherArtist = createUser(
                "song-delete-other@example.com", Role.ARTIST);

        Song song = createSong(
                owner, createAlbum(owner), "Protected Delete Song");

        mockMvc.perform(delete("/api/v1/songs/" + song.getPublicId())
                        .header("Authorization", bearer(otherArtist, "ARTIST")))
                .andExpect(status().isForbidden());
    }

    @Test
    void artistCannotUpdateAnotherArtistsAlbum() throws Exception {
        AppUser owner = createUser(
                "album-protected-owner@example.com", Role.ARTIST);
        AppUser otherArtist = createUser(
                "album-protected-other@example.com", Role.ARTIST);

        Album album = createAlbum(owner);

        mockMvc.perform(patch("/api/v1/albums/" + album.getPublicId())
                        .header("Authorization", bearer(otherArtist, "ARTIST"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"name": "Unauthorized Rename"}
                        """))
                .andExpect(status().isForbidden());
    }
    @Test
    void artistCannotDeleteAnotherArtistsAlbum() throws Exception {
        AppUser owner = createUser(
                "album-delete-owner@example.com", Role.ARTIST);
        AppUser otherArtist = createUser(
                "album-delete-other@example.com", Role.ARTIST);

        Album album = createAlbum(owner);

        mockMvc.perform(delete("/api/v1/albums/" + album.getPublicId())
                        .header("Authorization", bearer(otherArtist, "ARTIST")))
                .andExpect(status().isForbidden());
    }
    @Test
    void userCannotUpdateAnotherUsersPlaylist() throws Exception {
        AppUser owner = createUser(
                "playlist-protected-owner@example.com", Role.USER);
        AppUser other = createUser(
                "playlist-protected-other@example.com", Role.USER);

        Playlist playlist = createPlaylist(owner, "Protected Playlist");

        mockMvc.perform(put("/api/v1/playlists/" + playlist.getPublicId())
                        .header("Authorization", bearer(other, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "name": "Unauthorized Rename",
                          "description": "Attempted takeover"
                        }
                        """))
                .andExpect(status().isNotFound());
    }
    @Test
    void userCannotDeleteAnotherUsersPlaylist() throws Exception {
        AppUser owner = createUser(
                "playlist-delete-protected-owner@example.com", Role.USER);
        AppUser other = createUser(
                "playlist-delete-protected-other@example.com", Role.USER);

        Playlist playlist = createPlaylist(owner, "Protected Playlist");

        mockMvc.perform(delete("/api/v1/playlists/" + playlist.getPublicId())
                        .header("Authorization", bearer(other, "USER")))
                .andExpect(status().isNotFound());
    }
    @Test
    void usersCannotSeeEachOthersFavorites() throws Exception {
        AppUser user1 = createUser(
                "favorites-isolation-1@example.com", Role.USER);
        AppUser user2 = createUser(
                "favorites-isolation-2@example.com", Role.USER);
        AppUser artist = createUser(
                "favorites-isolation-artist@example.com", Role.ARTIST);

        Song song = createSong(
                artist, createAlbum(artist), "Private Favorite");

        mockMvc.perform(put("/api/v1/me/favorites/" + song.getPublicId())
                        .header("Authorization", bearer(user1, "USER")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/me/favorites")
                        .header("Authorization", bearer(user2, "USER")))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }


    @Test
    void adminCanDeleteSong() throws Exception {
        AppUser owner = createUser("song-delete-owner@example.com", Role.ARTIST);
        AppUser admin = createUser("song-delete-admin@example.com", Role.ADMIN);
        Song song = createSong(owner, createAlbum(owner), "Delete This Song");

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