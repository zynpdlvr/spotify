
package com.example.spotify.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
@Getter
@Setter
@Entity
@Table(name = "album")
public class Album  extends BaseEntity{

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 120)
    private String artistName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    @OneToMany(mappedBy = "album", fetch = FetchType.LAZY)
    private Set<Song> songs = new LinkedHashSet<>();

    protected Album() {
    }

    public Album(String name, String description, String artistName) {
        this.name = name;
        this.description = description;
        this.artistName = artistName;
    }


    public void update(
            String name,
            String description,
            String artistName
    ) {
        this.name = name;
        this.description = description;
        this.artistName = artistName;
    }

    public void addSong(Song song) {
        if (song == null) {
            throw new IllegalArgumentException("Song cannot be null.");
        }

        if (song.getAlbum() == this) {
            return;
        }

        if (song.getAlbum() != null) {
            song.getAlbum().removeSong(song);
        }

        songs.add(song);
        song.setAlbum(this);
    }

    public void removeSong(Song song) {
        if (song != null && song.getAlbum() == this) {
            songs.remove(song);
            song.setAlbum(null);
        }
    }
}
