package com.example.spotify.controller;

import com.example.spotify.common.ApiPaths;
import com.example.spotify.dto.AuthDTO;
import com.example.spotify.dto.UserDTO;
import com.example.spotify.mapper.UserMapper;
import com.example.spotify.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiPaths.BASE + "/admin/users")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    private final UserService users;
    private final UserMapper mapper;

    public AdminUserController(UserService users, UserMapper mapper) {
        this.users = users;
        this.mapper = mapper;
    }

    @PatchMapping("/{publicId}/role")
    public AuthDTO.UserResponse updateRole(
            @PathVariable UUID publicId,
            @Valid @RequestBody UserDTO.UpdateRoleRequest request) {

        return mapper.toResponse(
                users.updateRole(publicId, request.role())
        );
    }
}