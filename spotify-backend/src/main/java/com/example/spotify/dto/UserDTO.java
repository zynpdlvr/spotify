package com.example.spotify.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import com.example.spotify.entity.Role;


public class UserDTO {
    private UserDTO() {}

    public record UpdateMeRequest(
            @Size(max = 80)
            String displayName,

            @Email
            @Size(max = 190)
            String email
    ) {}
    public record UpdateRoleRequest(
            @NotNull Role role
    ) {
    }
}
