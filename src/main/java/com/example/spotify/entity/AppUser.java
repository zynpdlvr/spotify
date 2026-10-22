
package com.example.spotify.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "app_user")
public class AppUser  extends BaseEntity {


    @Setter
    @Column(nullable = false, length = 80)
    private String displayName;

    @Setter
    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private Set<Playlist> playlists = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Favorite> favorites = new LinkedHashSet<>();

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    protected AppUser() {
    }

    public AppUser(String displayName, String email, String passwordHash) {
        this.displayName = displayName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public void updateProfile(String displayName, String email) {
        if (displayName != null) {
            this.displayName = displayName;
        }

        if (email != null) {
            this.email = email;
        }
    }

    public void updatePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Password hash cannot be null or blank."
            );
        }

        this.passwordHash = passwordHash;
    }

    public void updateRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null.");
        }

        this.role = role;
    }

    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
    }

    public void removePlaylist(Playlist playlist) {
        playlists.remove(playlist);
    }

    public void addFavorite(Favorite favorite) {
        favorites.add(favorite);
    }

    public void removeFavorite(Favorite favorite) {
        favorites.remove(favorite);
    }
}
