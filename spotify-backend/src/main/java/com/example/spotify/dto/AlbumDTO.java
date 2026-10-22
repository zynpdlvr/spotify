
        package com.example.spotify.dto;

import com.example.spotify.dto.SongDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

        public final class AlbumDTO {

    private AlbumDTO() {}

    public record AlbumRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 500) String description,
            @NotBlank @Size(max = 120) String artistName
    ) {}

    public record UpdateAlbumRequest(
            @Size(max = 120) String name,
            @Size(max = 500) String description,
            @Size(max = 120) String artistName
    ) {}

    public record AlbumResponse(
            UUID publicId,
            String name,
            String description,
            String artistName,
            List<SongDTO.SongResponse> songs
    ) {}
}