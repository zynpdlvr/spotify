
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
@Table(name = "song")
public class Song  extends BaseEntity{


    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 160)
    private String artist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private AppUser owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @Column(length = 1000)
    private String audioUrl;

    @ManyToMany(mappedBy = "songs", fetch = FetchType.LAZY)
    private Set<Playlist> playlists = new LinkedHashSet<>();

    @OneToMany(mappedBy = "song", fetch = FetchType.LAZY)
    private Set<Favorite> favorites = new LinkedHashSet<>();

    protected Song() {
    }

    public Song(String title, String artist, Album album, String audioUrl) {
        this.title = title;
        this.artist = artist;
        setAlbum(album);
        this.audioUrl = audioUrl;

    }


    public void update(
            String title,
            String artist,
            Album album,
            String audioUrl
    ) {
        this.title = title;
        this.artist = artist;
        setAlbum(album);
        this.audioUrl = audioUrl;
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

    public void setAlbum(Album album) {
        if (this.album == album) {
            return;
        }

        Album previousAlbum = this.album;
        this.album = album;

        if (previousAlbum != null) {
            previousAlbum.getSongs().remove(this);
        }

        if (album != null) {
            album.getSongs().add(this);
        }
    }
}
