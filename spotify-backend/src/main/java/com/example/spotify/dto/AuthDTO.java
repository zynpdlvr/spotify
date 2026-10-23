package com.example.spotify.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;
import com.example.spotify.entity.Role;

public final class AuthDTO {
    private AuthDTO() {}
    public record RegisterRequest(@NotBlank @Size(max=80) String displayName,
                                  @NotBlank @Email @Size(max=190) String email,
                                  @NotBlank @Size(min=8, max=72) String password) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, UserResponse user) {}
    public record UserResponse(
            UUID publicId,
            String displayName,
            String email,
            com.example.spotify.entity.Role role
    ) {}
}
