package com.example.spotify.controller;
import com.example.spotify.common.ApiPaths;
import com.example.spotify.dto.AuthDTO;
import com.example.spotify.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping(ApiPaths.BASE + "/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth=auth; }
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDTO.AuthResponse register(@Valid @RequestBody AuthDTO.RegisterRequest req) {
        return auth.register(req);
    }
    @PostMapping("/login")
    public AuthDTO.AuthResponse login(@Valid @RequestBody AuthDTO.LoginRequest req) { return auth.login(req); }
}
