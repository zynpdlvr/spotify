
package com.example.spotify.service;

import com.example.spotify.dto.PlaylistDTO;
import com.example.spotify.entity.AppUser;
import com.example.spotify.entity.Playlist;
import com.example.spotify.mapper.PlaylistMapper;
import com.example.spotify.repository.PlaylistRepository;
import com.example.spotify.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class PlaylistService {

    private final PlaylistRepository playlists;
    private final UserRepository users;
    private final SongService songs;
    private final PlaylistMapper playlistMapper;

    public PlaylistService(
            PlaylistRepository playlists,
            UserRepository users,
            SongService songs,
            PlaylistMapper playlistMapper) {
        this.playlists = playlists;
        this.users = users;
        this.songs = songs;
        this.playlistMapper = playlistMapper;
    }

    // Kullanıcının kendi playlist'lerini listeler.
    @Transactional(readOnly = true)
    public List<PlaylistDTO.PlaylistResponse> list(Long userId) {
        return playlists.findAllByOwner_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(playlistMapper::toResponse)
                .toList();
    }

    // Yeni playlist oluşturur.
    @Transactional
    public PlaylistDTO.PlaylistResponse create(
            Long userId,
            PlaylistDTO.PlaylistRequest request) {

        AppUser owner = users.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Kullanıcı bulunamadı."
                        )
                );

        Playlist playlist = new Playlist(
                request.name().trim(),
                request.description(),
                owner
        );

        return playlistMapper.toResponse(playlists.save(playlist));
    }

    // Playlist'i UUID ve sahiplik kontrolüyle getirir.
    @Transactional(readOnly = true)
    public PlaylistDTO.PlaylistResponse get(
            Long userId,
            UUID publicId) {

        return playlistMapper.toResponse(owned(userId, publicId));
    }

    // Playlist bilgilerini günceller.
    @Transactional
    public PlaylistDTO.PlaylistResponse update(
            Long userId,
            UUID publicId,
            PlaylistDTO.PlaylistRequest request) {

        Playlist playlist = owned(userId, publicId);

        playlist.update(
                request.name().trim(),
                request.description()
        );

        return playlistMapper.toResponse(playlist);
    }

    // Playlist'i siler.
    @Transactional
    public void delete(Long userId, UUID publicId) {
        playlists.delete(owned(userId, publicId));
    }

    // Playlist'e şarkı ekler.
    @Transactional
    public PlaylistDTO.PlaylistResponse addSong(
            Long userId,
            UUID playlistPublicId,
            UUID songPublicId) {

        Playlist playlist = owned(userId, playlistPublicId);

        playlist.addSong(songs.find(songPublicId));

        return playlistMapper.toResponse(playlist);
    }

    // Playlist'ten şarkı çıkarır.
    @Transactional
    public PlaylistDTO.PlaylistResponse removeSong(
            Long userId,
            UUID playlistPublicId,
            UUID songPublicId) {

        Playlist playlist = owned(userId, playlistPublicId);

        playlist.removeSong(songs.find(songPublicId));

        return playlistMapper.toResponse(playlist);
    }

    // Playlist'in varlığını ve kullanıcının sahipliğini doğrular.
    private Playlist owned(Long userId, UUID publicId) {
        return playlists.findWithSongsByPublicIdAndOwner_Id(
                        publicId,
                        userId
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Çalma listesi bulunamadı."
                        )
                );
    }
}
