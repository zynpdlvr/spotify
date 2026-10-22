
package com.example.spotify.service;

import com.example.spotify.dto.SongDTO;
import com.example.spotify.entity.Favorite;
import com.example.spotify.mapper.SongMapper;
import com.example.spotify.repository.FavoriteRepository;
import com.example.spotify.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class FavoriteService {

    private final FavoriteRepository favorites;
    private final UserRepository users;
    private final SongService songs;
    private final SongMapper songMapper;

    public FavoriteService(
            FavoriteRepository favorites,
            UserRepository users,
            SongService songs,
            SongMapper songMapper) {
        this.favorites = favorites;
        this.users = users;
        this.songs = songs;
        this.songMapper = songMapper;
    }

    // Giriş yapan kullanıcının favorilerini listeler.
    @Transactional(readOnly = true)
    public List<SongDTO.SongResponse> list(Long userId) {
        return favorites.findAllByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(favorite -> songMapper.toResponse(favorite.getSong()))
                .toList();
    }

    // Şarkıyı favorilere ekler.
    @Transactional
    public void add(Long userId, UUID songPublicId) {

        // Aynı şarkı zaten favorilerdeyse tekrar ekleme.
        if (favorites
                .findByUser_IdAndSong_PublicId(userId, songPublicId)
                .isPresent()) {
            return;
        }

        var user = users.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Kullanıcı bulunamadı."
                        )
                );

        var song = songs.find(songPublicId);

        favorites.save(new Favorite(user, song));
    }

    // Şarkıyı favorilerden çıkarır.
    @Transactional
    public void remove(Long userId, UUID songPublicId) {

        Favorite favorite = favorites
                .findByUser_IdAndSong_PublicId(userId, songPublicId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Favori bulunamadı."
                        )
                );

        favorite.detachFromRelations();
        favorites.delete(favorite);
    }
}
