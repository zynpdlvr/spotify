package com.example.spotify.config;

import com.example.spotify.common.ApiPaths;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecretKey jwtSecretKey(@Value("${app.jwt.secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);

        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least 32 bytes."
            );
        }

        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey key) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey key) {
        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // Kayıt ve giriş herkese açık
                        .requestMatchers(
                                ApiPaths.BASE + "/auth/**"
                        ).permitAll()

                        .requestMatchers(
                                "/actuator/health",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()

                        // Şarkı ve albümleri herkes okuyabilir
                        .requestMatchers(
                                HttpMethod.GET,
                                ApiPaths.BASE + "/songs",
                                ApiPaths.BASE + "/songs/**",
                                ApiPaths.BASE + "/albums",
                                ApiPaths.BASE + "/albums/**"
                        ).permitAll()

                        // Şarkı oluşturma: ADMIN ve ARTIST
                        .requestMatchers(
                                HttpMethod.POST,
                                ApiPaths.BASE + "/songs",
                                ApiPaths.BASE + "/songs/**"
                        ).hasAnyAuthority("ADMIN", "ARTIST")

                        // Şarkı güncelleme: ADMIN ve ARTIST
                        .requestMatchers(
                                HttpMethod.PATCH,
                                ApiPaths.BASE + "/songs/**"
                        ).hasAnyAuthority("ADMIN", "ARTIST")

                        .requestMatchers(
                                HttpMethod.PUT,
                                ApiPaths.BASE + "/songs/**"
                        ).hasAnyAuthority("ADMIN", "ARTIST")

                        // Şarkı silme: ADMIN ve ARTIST
                        .requestMatchers(
                                HttpMethod.DELETE,
                                ApiPaths.BASE + "/songs/**"
                        ).hasAnyAuthority("ADMIN", "ARTIST")

                        // Albüm oluşturma: ADMIN ve ARTIST
                        .requestMatchers(
                                HttpMethod.POST,
                                ApiPaths.BASE + "/albums",
                                ApiPaths.BASE + "/albums/**"
                        ).hasAnyAuthority("ADMIN", "ARTIST")

                        // Albüm güncelleme: ADMIN ve ARTIST
                        .requestMatchers(
                                HttpMethod.PATCH,
                                ApiPaths.BASE + "/albums/**"
                        ).hasAnyAuthority("ADMIN", "ARTIST")

                        .requestMatchers(
                                HttpMethod.PUT,
                                ApiPaths.BASE + "/albums/**"
                        ).hasAnyAuthority("ADMIN", "ARTIST")

                        // Albüm silme: ADMIN ve ARTIST
                        .requestMatchers(
                                HttpMethod.DELETE,
                                ApiPaths.BASE + "/albums/**"
                        ).hasAnyAuthority("ADMIN", "ARTIST")

                        .requestMatchers(ApiPaths.BASE + "/admin/**")
                        .hasAuthority("ADMIN")

                        // Geri kalan endpoint'ler giriş gerektirir
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                );

        return http.build();
    }

    @Bean
    Converter<Jwt, ? extends AbstractAuthenticationToken>
    jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("authorities");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}