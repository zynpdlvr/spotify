package com.example.spotify.service;

import com.example.spotify.dto.AuthDTO;
import com.example.spotify.entity.AppUser;
import com.example.spotify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import com.example.spotify.mapper.UserMapper;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtEncoder jwtEncoder;
    private final long expirationMinutes;
    private final UserMapper userMapper;

    public AuthService(
            UserRepository users,
            PasswordEncoder encoder,
            JwtEncoder jwtEncoder,
            UserMapper userMapper,
            @Value("${app.jwt.expiration-minutes:60}") long expirationMinutes) {
        this.users = users;
        this.encoder = encoder;
        this.jwtEncoder = jwtEncoder;
        this.expirationMinutes = expirationMinutes;
        this.userMapper= userMapper;
    }

    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu e-posta zaten kayıtlı.");
        AppUser user = users.save(new AppUser(req.displayName().trim(), email, encoder.encode(req.password())));
        return tokenFor(user);
    }

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest req) {
        AppUser user = users.findByEmailIgnoreCase(req.email().trim())
            .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-posta veya parola hatalı."));
        return tokenFor(user);
    }

    private AuthDTO.AuthResponse tokenFor(AppUser user) {
        Instant now = Instant.now();
        Instant expires = now.plus(expirationMinutes, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("spotify-backend")
                .issuedAt(now)
                .expiresAt(expires)
                .subject(user.getId().toString())
                .claim("publicId", user.getPublicId().toString())
                .claim("email", user.getEmail())
                .claim("authorities", List.of(user.getRole().name()))
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new AuthDTO.AuthResponse(
                token, "Bearer",
                expirationMinutes * 60,
                userMapper.toResponse(user));
    }
}
