
package com.example.spotify.controller;

import com.example.spotify.common.ApiPaths;
import com.example.spotify.dto.SongDTO;
import com.example.spotify.service.SongService;

import jakarta.validation.Valid;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@RestController
@RequestMapping(ApiPaths.BASE + "/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    // Şarkıları listeler; q parametresi isteğe bağlı aramadır.
    @GetMapping
    public CollectionModel<EntityModel<SongDTO.SongResponse>> all(
            @RequestParam(required = false) String q, Pageable pageable) {

        List<EntityModel<SongDTO.SongResponse>> songs =
                songService.all(q,pageable)
                        .stream()
                        .map(this::toModel)
                        .toList();

        return CollectionModel.of(
                songs,
                linkTo(methodOn(SongController.class).all(q,pageable))
                        .withSelfRel()
        );
    }

    // Bir şarkının detayını getirir.
    @GetMapping("/{publicId}")
    public EntityModel<SongDTO.SongResponse> get(
            @PathVariable UUID publicId) {

        return toModel(songService.get(publicId));
    }

    // Şarkı oluşturur.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ADMIN','ARTIST')")
    public EntityModel<SongDTO.SongResponse> create(
            @Valid @RequestBody SongDTO.SongRequest request,
            JwtAuthenticationToken authentication) {

        SongDTO.SongResponse created =
                songService.create(request, authentication);

        return toModel(created);
    }

    // Şarkının bazı alanlarını günceller.
    @PatchMapping("/{publicId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','ARTIST')")
    public EntityModel<SongDTO.SongResponse> updateSong(
            @PathVariable UUID publicId,
            @Valid @RequestBody SongDTO.UpdateSongRequest request,
            JwtAuthenticationToken authentication) {

        return toModel(
                songService.updateSong(publicId, request, authentication)
        );
    }

    // Şarkının kaynak temsilini bütünüyle günceller.
    @PutMapping("/{publicId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','ARTIST')")
    public EntityModel<SongDTO.SongResponse> update(
            @PathVariable UUID publicId,
            @Valid @RequestBody SongDTO.SongRequest request,
            JwtAuthenticationToken authentication) {

        return toModel(
                songService.update(publicId, request, authentication)
        );
    }

    // Şarkıyı siler.
    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('ADMIN','ARTIST')")
    public void delete(
            @PathVariable UUID publicId,
            JwtAuthenticationToken authentication) {

        songService.delete(publicId, authentication);
    }
    // DTO'ya HATEOAS bağlantıları ekler.
    private EntityModel<SongDTO.SongResponse> toModel(
            SongDTO.SongResponse song) {

        return EntityModel.of(
                song,
                linkTo(methodOn(SongController.class)
                        .get(song.publicId()))
                        .withSelfRel(),
                linkTo(methodOn(SongController.class)
                        .all(null, Pageable.unpaged()))
                        .withRel("songs")
        );
    }
}
