package com.example.spotify.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class PlaylistDTO {
    private PlaylistDTO() {}
    public record PlaylistRequest(
            @NotBlank @Size(max=120)
            String name,
            @Size(max=500)
            String description) {}
    public record PlaylistResponse(
            UUID publicId,
            String name,
            String description,
            UUID ownerPublicId,
            Instant createdAt,
            List<SongDTO.SongResponse> songs) {}
}
