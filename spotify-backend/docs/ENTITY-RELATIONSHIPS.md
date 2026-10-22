# Entity relationship diagramı

```mermaid
erDiagram
    APP_USER ||--o{ PLAYLIST : owns
    APP_USER ||--o{ FAVORITE : has
    SONG ||--o{ FAVORITE : favorited
    PLAYLIST }o--o{ SONG : contains

    APP_USER {
        bigint id PK
        varchar display_name
        varchar email UK
        varchar password_hash
        timestamp created_at
    }
    PLAYLIST {
        bigint id PK
        varchar name
        varchar description
        bigint owner_id FK
        timestamp created_at
    }
    SONG {
        bigint id PK
        varchar title
        varchar artist
        varchar album
        varchar audio_url
        timestamp created_at
    }
    FAVORITE {
        bigint id PK
        bigint user_id FK
        bigint song_id FK
        timestamp created_at
    }
```

## JPA ilişkileri

- `AppUser` -> `Playlist`: çift yönlü `@OneToMany` / `@ManyToOne`; `Playlist.owner` sahip taraftır.
- `AppUser` -> `Favorite`: çift yönlü `@OneToMany` / `@ManyToOne`; `Favorite.user` sahip taraftır.
- `Song` -> `Favorite`: çift yönlü `@OneToMany` / `@ManyToOne`; `Favorite.song` sahip taraftır.
- `Playlist` <-> `Song`: çift yönlü `@ManyToMany`; `Playlist.songs` sahip taraftır ve `playlist_songs` bağlantı tablosunu yönetir.

`app_users.email` benzersizdir. `favorites` tablosunda `(user_id, song_id)` ikilisi benzersizdir; böylece aynı kullanıcı aynı şarkıyı birden fazla kez favorileyemez.

Şarkı silme işlemi transaction içinde önce playlist bağlantılarını ve favori kayıtlarını kaldırır, ardından şarkıyı siler. Böylece foreign-key kısıtlarından kaynaklanabilecek silme hataları önlenir.
