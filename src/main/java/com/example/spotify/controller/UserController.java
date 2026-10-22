
package com.example.spotify.controller;

import com.example.spotify.common.ApiPaths;
import com.example.spotify.dto.AuthDTO;
import com.example.spotify.dto.UserDTO;
import com.example.spotify.entity.AppUser;
import com.example.spotify.mapper.UserMapper;
import com.example.spotify.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ApiPaths.BASE + "/users")
public class UserController {

    private final UserService users;
    private final UserMapper userMapper;

    public UserController(UserService users, UserMapper userMapper) {
        this.users = users;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public AuthDTO.UserResponse getMe(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = currentUserId(jwt);

        return userMapper.toResponse(users.getMe(userId));
    }

    @PatchMapping("/me")
    public AuthDTO.UserResponse updateMe(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserDTO.UpdateMeRequest request
    ) {
        Long userId = currentUserId(jwt);

        AppUser updated = users.updateMe(userId, request);

        return userMapper.toResponse(updated);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe(
            @AuthenticationPrincipal Jwt jwt
    ) {
        users.deleteMe(currentUserId(jwt));
    }

    private Long currentUserId(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Kimlik doğrulaması gerekli."
            );
        }

        try {
            return Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Geçersiz kullanıcı kimliği."
            );
        }
    }
}
