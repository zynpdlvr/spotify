
package com.example.spotify.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "playlist")
public class Playlist  extends BaseEntity {


    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @ManyToMany
    @JoinTable(
            name = "playlist_song",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id"),
            uniqueConstraints = @UniqueConstraint(
                    columnNames = {"playlist_id", "song_id"}
            )
    )
    private Set<Song> songs = new LinkedHashSet<>();
    protected Playlist() {
    }

    public Playlist(String name, String description, AppUser owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null.");
        }

        this.name = name;
        this.description = description;
        this.owner = owner;

        owner.addPlaylist(this);
    }


    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void addSong(Song song) {
        if (song == null) {
            throw new IllegalArgumentException("Song cannot be null.");
        }

        if (songs.add(song)) {
            song.addPlaylist(this);
        }
    }

    public void removeSong(Song song) {
        if (song != null && songs.remove(song)) {
            song.removePlaylist(this);
        }
    }
}
