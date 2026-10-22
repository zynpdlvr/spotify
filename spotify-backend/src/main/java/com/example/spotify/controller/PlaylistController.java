
package com.example.spotify.controller;

import com.example.spotify.common.ApiPaths;
import com.example.spotify.dto.PlaylistDTO;
import com.example.spotify.service.PlaylistService;

import jakarta.validation.Valid;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.BASE + "/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    // Kullanıcının dahili ID'sini doğrulanmış JWT'den alır.
    private Long userId(JwtAuthenticationToken auth) {
        return Long.valueOf(auth.getToken().getSubject());
    }

    // Playlist yanıtına HATEOAS bağlantıları ekler.
    private EntityModel<PlaylistDTO.PlaylistResponse> toModel(
            PlaylistDTO.PlaylistResponse playlist) {

        return EntityModel.of(
                playlist,
                linkTo(methodOn(PlaylistController.class)
                        .get(playlist.publicId(), null))
                        .withSelfRel(),
                linkTo(methodOn(PlaylistController.class)
                        .list(null))
                        .withRel("my-playlists"),
                linkTo(methodOn(PlaylistController.class)
                        .addSong(playlist.publicId(), UUID.randomUUID(), null))
                        .withRel("add-song-template")
                        .withType("PUT")
        );
    }

    // Giriş yapan kullanıcının playlist'lerini listeler.
    @GetMapping
    public CollectionModel<EntityModel<PlaylistDTO.PlaylistResponse>> list(
            JwtAuthenticationToken auth) {

        List<EntityModel<PlaylistDTO.PlaylistResponse>> playlists =
                playlistService.list(userId(auth))
                        .stream()
                        .map(this::toModel)
                        .toList();

        return CollectionModel.of(
                playlists,
                linkTo(methodOn(PlaylistController.class).list(null))
                        .withSelfRel()
        );
    }

    // Playlist oluşturur.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<PlaylistDTO.PlaylistResponse> create(
            @Valid @RequestBody PlaylistDTO.PlaylistRequest request,
            JwtAuthenticationToken auth) {

        return toModel(playlistService.create(userId(auth), request));
    }

    // Playlist detayını getirir.
    @GetMapping("/{publicId}")
    public EntityModel<PlaylistDTO.PlaylistResponse> get(
            @PathVariable UUID publicId,
            JwtAuthenticationToken auth) {

        return toModel(playlistService.get(userId(auth), publicId));
    }

    // Playlist bilgilerini günceller.
    @PutMapping("/{publicId}")
    public EntityModel<PlaylistDTO.PlaylistResponse> update(
            @PathVariable UUID publicId,
            @Valid @RequestBody PlaylistDTO.PlaylistRequest request,
            JwtAuthenticationToken auth) {

        return toModel(
                playlistService.update(userId(auth), publicId, request));
    }

    // Playlist'i siler.
    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID publicId,
            JwtAuthenticationToken auth) {

        playlistService.delete(userId(auth), publicId);
    }

    // Playlist'e şarkı ekler.
    @PutMapping("/{publicId}/songs/{songPublicId}")
    public EntityModel<PlaylistDTO.PlaylistResponse> addSong(
            @PathVariable UUID publicId,
            @PathVariable UUID songPublicId,
            JwtAuthenticationToken auth) {

        return toModel(
                playlistService.addSong(userId(auth), publicId, songPublicId));
    }

    // Playlist'ten şarkı çıkarır.
    @DeleteMapping("/{publicId}/songs/{songPublicId}")
    public EntityModel<PlaylistDTO.PlaylistResponse> removeSong(
            @PathVariable UUID publicId,
            @PathVariable UUID songPublicId,
            JwtAuthenticationToken auth) {

        return toModel(
                playlistService.removeSong(
                        userId(auth), publicId, songPublicId));
    }
}
