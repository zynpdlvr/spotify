
package com.example.spotify.entity;

import jakarta.persistence.*;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "favorite",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "song_id"}
        )
)
public class Favorite  extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "song_id", nullable = false)
    private Song song;

    protected Favorite() {
    }

    public Favorite(AppUser user, Song song) {
        if (user == null || song == null) {
            throw new IllegalArgumentException(
                    "User and song cannot be null."
            );
        }

        this.user = user;
        this.song = song;

        user.addFavorite(this);
        song.addFavorite(this);
    }
    public void detachFromRelations() {
        user.removeFavorite(this);
        song.removeFavorite(this);
    }
}
