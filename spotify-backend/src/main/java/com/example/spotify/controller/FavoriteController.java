
package com.example.spotify.controller;

import com.example.spotify.common.ApiPaths;
import com.example.spotify.dto.SongDTO;
import com.example.spotify.service.FavoriteService;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.BASE + "/me/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // Kullanıcının ID'sini doğrulanmış JWT üzerinden alır.
    private Long userId(JwtAuthenticationToken auth) {
        return Long.valueOf(auth.getToken().getSubject());
    }

    // Giriş yapan kullanıcının favori şarkılarını listeler.
    @GetMapping
    public List<SongDTO.SongResponse> list(
            JwtAuthenticationToken auth) {

        return favoriteService.list(userId(auth));
    }

    // Bir şarkıyı favorilere ekler.
    @PutMapping("/{songPublicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void add(
            @PathVariable UUID songPublicId,
            JwtAuthenticationToken auth) {

        favoriteService.add(userId(auth), songPublicId);
    }

    // Bir şarkıyı favorilerden çıkarır.
    @DeleteMapping("/{songPublicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @PathVariable UUID songPublicId,
            JwtAuthenticationToken auth) {

        favoriteService.remove(userId(auth), songPublicId);
    }
}
