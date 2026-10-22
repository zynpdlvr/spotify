
package com.example.spotify.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class SongDTO {

    private SongDTO() {}

    // POST ve PUT: yeni şarkı oluşturma veya tüm alanları güncelleme
    public record SongRequest(
            @NotBlank
            @Size(max = 160)
            String title,

            @NotBlank
            @Size(max = 160)
            String artist,

            UUID albumPublicId,

            @Size(max = 1000)
            String audioUrl
    ) {}

    // API yanıtı
    public record SongResponse(
            UUID publicId,
            String title,
            String artist,
            UUID albumPublicId,
            String albumName,
            String audioUrl,
            Instant createdAt
    ) {}

    // PATCH: yalnızca gönderilen alanları güncelleme
    public record UpdateSongRequest(
            @Size(max = 160)
            String title,

            @Size(max = 160)
            String artist,

            UUID albumPublicId,

            @Size(max = 1000)
            String audioUrl
    ) {}
}
