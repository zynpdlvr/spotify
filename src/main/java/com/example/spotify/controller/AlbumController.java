package com.example.spotify.controller;

import com.example.spotify.common.ApiPaths;
import com.example.spotify.dto.AlbumDTO;
import com.example.spotify.service.AlbumService;

import jakarta.validation.Valid;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(ApiPaths.BASE + "/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    // Herkes albümleri listeleyebilir.
    @GetMapping
    public CollectionModel<EntityModel<AlbumDTO.AlbumResponse>> all() {

        List<EntityModel<AlbumDTO.AlbumResponse>> albums =
                albumService.all()
                        .stream()
                        .map(this::toModel)
                        .toList();

        return CollectionModel.of(
                albums,
                linkTo(methodOn(AlbumController.class).all())
                        .withSelfRel()
        );
    }

    // Herkes albüm detayını ve şarkılarını görebilir.
    @GetMapping("/{publicId}")
    public EntityModel<AlbumDTO.AlbumResponse> get(
            @PathVariable UUID publicId) {

        return toModel(albumService.get(publicId));
    }

    // ADMIN ve ARTIST albüm oluşturabilir.
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','ARTIST')")
    public ResponseEntity<EntityModel<AlbumDTO.AlbumResponse>> create(
            @Valid @RequestBody AlbumDTO.AlbumRequest request,
            JwtAuthenticationToken authentication) {

        UUID userPublicId = UUID.fromString(
                authentication.getToken().getClaimAsString("publicId")
        );
        boolean admin = isAdmin(authentication);

        AlbumDTO.AlbumResponse created =
                albumService.create(request, userPublicId, admin);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{publicId}")
                .buildAndExpand(created.publicId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(toModel(created));
    }

    // ADMIN veya albüm sahibi ARTIST güncelleyebilir.
    @PatchMapping("/{publicId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','ARTIST')")
    public EntityModel<AlbumDTO.AlbumResponse> update(
            @PathVariable UUID publicId,
            @Valid @RequestBody AlbumDTO.UpdateAlbumRequest request,
            JwtAuthenticationToken authentication) {

        UUID userPublicId = UUID.fromString(
                authentication.getToken().getClaimAsString("publicId")
        );
        boolean admin = isAdmin(authentication);

        return toModel(
                albumService.update(
                        publicId,
                        request,
                        userPublicId,
                        admin
                )
        );
    }

    // ADMIN veya albüm sahibi ARTIST silebilir.
    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','ARTIST')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID publicId,
            JwtAuthenticationToken authentication) {

        UUID userPublicId = UUID.fromString(
                authentication.getToken().getClaimAsString("publicId")
        );
        boolean admin = isAdmin(authentication);

        albumService.delete(publicId, userPublicId, admin);

        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(JwtAuthenticationToken authentication) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ADMIN"));
    }

    // DTO'yu HATEOAS bağlantılarıyla birlikte sunar.
    private EntityModel<AlbumDTO.AlbumResponse> toModel(
            AlbumDTO.AlbumResponse album) {

        return EntityModel.of(
                album,
                linkTo(methodOn(AlbumController.class)
                        .get(album.publicId()))
                        .withSelfRel(),
                linkTo(methodOn(AlbumController.class).all())
                        .withRel("albums")
        );
    }
}