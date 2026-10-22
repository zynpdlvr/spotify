
package com.example.spotify.service;
import com.example.spotify.dto.UserDTO;
import com.example.spotify.entity.*;
import com.example.spotify.repository.FavoriteRepository;
import com.example.spotify.repository.PlaylistRepository;
import com.example.spotify.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository users;
    private final FavoriteRepository favorites;
    private final PlaylistRepository playlists;

    public UserService(
            UserRepository users,
            FavoriteRepository favorites,
            PlaylistRepository playlists) {
        this.users = users;
        this.favorites = favorites;
        this.playlists = playlists;
    }

    @Transactional(readOnly = true)
    public AppUser getMe(Long userId) {
        return findUser(userId);
    }

    @Transactional
    public AppUser updateMe(Long userId, UserDTO.UpdateMeRequest req) {
        AppUser user = findUser(userId);

        if (req.displayName() == null && req.email() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Güncellenecek en az bir alan gönderilmelidir."
            );
        }

        if (req.displayName() != null) {
            String displayName = req.displayName().trim();

            if (displayName.isBlank() || displayName.length() > 80) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Görünen ad boş olamaz ve en fazla 80 karakter olabilir."
                );
            }

            user.setDisplayName(displayName);
        }

        if (req.email() != null) {
            String email = req.email().trim().toLowerCase();

            if (email.isBlank() || email.length() > 190) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Geçerli bir e-posta girilmelidir."
                );
            }

            boolean emailChanged =
                    !email.equalsIgnoreCase(user.getEmail());

            if (emailChanged && users.existsByEmailIgnoreCase(email)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Bu e-posta zaten kayıtlı."
                );
            }

            user.setEmail(email);
        }

        return user;
    }

    @Transactional
    public AppUser updateRole(UUID publicId, Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        AppUser user = users.findByPublicId(publicId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found"));

        user.setRole(role);

        return user;
    }

    @Transactional
    public void deleteMe(Long userId) {
        AppUser user = users.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        // 1. Kullanıcının favorilerini sil
        List<Favorite> userFavorites =
                favorites.findAllByUser_Id(userId);

        for (Favorite favorite : userFavorites) {
            favorite.detachFromRelations();
            favorites.delete(favorite);
        }

        // 2. Kullanıcının playlist'lerini sil
        List<Playlist> userPlaylists =
                playlists.findAllByOwner_Id(userId);

        for (Playlist playlist : userPlaylists) {
            // Playlist ile şarkılar arasındaki ilişkileri kaldır.
            for (Song song : new ArrayList<>(playlist.getSongs())) {
                playlist.removeSong(song);
            }

            user.removePlaylist(playlist);
            playlists.delete(playlist);
        }

        // 3. Kullanıcıyı sil
        users.delete(user);
    }

    private AppUser findUser(Long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Kullanıcı bulunamadı."
                ));
    }


}