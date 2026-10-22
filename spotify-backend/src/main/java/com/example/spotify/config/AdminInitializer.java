
package com.example.spotify.config;

import com.example.spotify.entity.AppUser;
import com.example.spotify.entity.Role;
import com.example.spotify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner createAdmin(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.password}") String password,
            @Value("${app.admin.display-name:Admin}") String displayName
    ) {
        return args -> {
            if (users.findByEmail(email).isPresent()) {
                return;
            }

            AppUser admin = new AppUser(
                    displayName,
                    email,
                    passwordEncoder.encode(password)
            );

            admin.setRole(Role.ADMIN);
            users.save(admin);
        };
    }
}