package com.example.spotify.integration;

import com.example.spotify.entity.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends IntegrationTestSupport {

    @Test
    void registerReturnsCreatedAndDoesNotExposePassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "displayName": "New User",
                              "email": "new-user@example.com",
                              "password": "Password123!"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("new-user@example.com"))
                .andExpect(jsonPath("$.user.publicId").isNotEmpty())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        String body = """
            {
              "displayName": "New User",
              "email": "duplicate@example.com",
              "password": "Password123!"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidRegistrationReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "displayName": "",
                              "email": "not-an-email",
                              "password": "short"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturnsTokenForValidCredentials() throws Exception {
        AppUser user = new AppUser(
                "Login User",
                "login@example.com",
                passwordEncoder.encode("Password123!")
        );

        users.saveAndFlush(user);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "login@example.com",
                              "password": "Password123!"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("login@example.com"));
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        AppUser user = new AppUser(
                "Login User",
                "wrong-password@example.com",
                passwordEncoder.encode("Password123!")
        );

        users.saveAndFlush(user);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "wrong-password@example.com",
                              "password": "WrongPassword123!"
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithInvalidEmailReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "invalid",
                              "password": "Password123!"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }
}