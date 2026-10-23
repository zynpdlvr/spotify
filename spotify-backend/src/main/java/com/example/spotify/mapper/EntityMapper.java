
package com.example.spotify.mapper;

import com.example.spotify.dto.AlbumDTO;
import com.example.spotify.dto.AuthDTO;
import com.example.spotify.dto.PlaylistDTO;
import com.example.spotify.dto.SongDTO;
import com.example.spotify.entity.Album;
import com.example.spotify.entity.AppUser;
import com.example.spotify.entity.Playlist;
import com.example.spotify.entity.Song;

import java.util.List;

public final class EntityMapper {

    private EntityMapper() {}

    public static AuthDTO.UserResponse toUser(AppUser user) {
        return new AuthDTO.UserResponse(
                user.getPublicId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public static SongDTO.SongResponse toSong(Song song) {
        Album album = song.getAlbum();

        return new SongDTO.SongResponse(
                song.getPublicId(),
                song.getTitle(),
                song.getArtist(),
                album != null ? album.getPublicId() : null,
                album != null ? album.getName() : null,
                song.getAudioUrl(),
                song.getCreatedAt()
        );
    }

    public static AlbumDTO.AlbumResponse toAlbum(Album album) {
        List<SongDTO.SongResponse> songs = album.getSongs()
                .stream()
                .map(EntityMapper::toSong)
                .toList();

        return new AlbumDTO.AlbumResponse(
                album.getPublicId(),
                album.getName(),
                album.getDescription(),
                album.getArtistName(),
                songs
        );
    }

    public static PlaylistDTO.PlaylistResponse toPlaylist(Playlist playlist) {
        return new PlaylistDTO.PlaylistResponse(
                playlist.getPublicId(),
                playlist.getName(),
                playlist.getDescription(),
                playlist.getOwner().getPublicId(),
                playlist.getCreatedAt(),
                playlist.getSongs()
                        .stream()
                        .map(EntityMapper::toSong)
                        .toList()
        );
    }
}
