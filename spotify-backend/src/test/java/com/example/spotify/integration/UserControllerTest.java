package com.example.spotify.integration;

import com.example.spotify.entity.AppUser;
import com.example.spotify.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest extends IntegrationTestSupport {

    @Test
    void getMeReturnsOnlyPublicProfileFields() throws Exception {
        AppUser user = createUser("profile@example.com", Role.USER);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", bearer(user, "USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId")
                        .value(user.getPublicId().toString()))
                .andExpect(jsonPath("$.email").value("profile@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void getMeRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patchMeUpdatesDisplayNameAndEmail() throws Exception {
        AppUser user = createUser("before@example.com", Role.USER);

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", bearer(user, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "displayName": "Updated Name",
                              "email": "after@example.com"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("after@example.com"));
    }

    @Test
    void patchMeRejectsInvalidEmail() throws Exception {
        AppUser user = createUser("valid-email@example.com", Role.USER);

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", bearer(user, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email": "not-an-email"}
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchMeRejectsBlankDisplayName() throws Exception {
        AppUser user = createUser("blank-name@example.com", Role.USER);

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", bearer(user, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"displayName": "   "}
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchMeRejectsDuplicateEmail() throws Exception {
        AppUser user = createUser("first@example.com", Role.USER);
        createUser("taken@example.com", Role.USER);

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", bearer(user, "USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email": "taken@example.com"}
                            """))
                .andExpect(status().isConflict());
    }

    @Test
    void patchMeRequiresAuthentication() throws Exception {
        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"displayName": "No Token"}
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteMeDeletesAccount() throws Exception {
        AppUser user = createUser("delete-me@example.com", Role.USER);

        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", bearer(user, "USER")))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions
                .assertThat(users.findById(user.getId()))
                .isEmpty();
    }
}