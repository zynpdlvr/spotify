import Cover from "./Cover";

export default function SongTable({
  songs,
  loading,
  error,
  favoritesLoading,
  favoritesError,
  favoriteActionLoading,
  favorites,
  activeSong,
  playing,
  activeMenu,
  search,
  selectedAlbum,
  selectedPlaylistData,
  selectedPlaylistSongIds,
  onPlaySong,
  onToggleFavorite,
  onAddSongToPlaylist,
  onRemoveSongFromPlaylist,
}) {
  return (
    <>
      {favoritesError && <p className="empty-state" role="alert">{favoritesError}</p>}

      <div className="section-heading songs-heading">
        <h2>
          {search
            ? "Bulunan şarkılar"
            : activeMenu === "Beğenilen Şarkılar"
              ? "Beğenilen Şarkılar"
              : selectedAlbum
                ? selectedAlbum.name
                : selectedPlaylistData
                  ? "Playlist şarkıları"
                  : activeMenu === "Kitaplığın"
                    ? "Şarkı kitaplığı"
                    : "Popüler şarkılar"}
        </h2>
        <span className="muted">{songs.length} şarkı</span>
      </div>

      <div className="song-table">
        <div className="song-table-header">
          <span>#</span><span>Şarkı</span><span className="duration">Süre</span><span />
        </div>

        {loading && <p className="empty-state">Şarkılar yükleniyor...</p>}
        {!loading && error && (
          <p className="empty-state" role="alert">Şarkılar yüklenemedi: {error}</p>
        )}
        {!loading && !error && favoritesLoading && activeMenu === "Beğenilen Şarkılar" && (
          <p className="empty-state">Favoriler yükleniyor...</p>
        )}
        {!loading && !error && songs.length === 0 && (
          <p className="empty-state">
            {activeMenu === "Beğenilen Şarkılar"
              ? "Henüz favori şarkın yok."
              : selectedAlbum
                ? "Bu albümde gösterilecek şarkı bulunamadı."
                : selectedPlaylistData
                  ? "Bu playlist'te henüz şarkı yok."
                  : search
                    ? "Bu aramayla eşleşen şarkı bulunamadı."
                    : "Henüz şarkı bulunamadı."}
          </p>
        )}

        {!loading && !error && songs.map((song, index) => (
          <div
            className={`song-row ${activeSong?.id === song.id ? "currently-selected" : ""}`}
            key={song.id}
          >
            <button className="row-number" onClick={() => onPlaySong(song)} aria-label={`${song.title} şarkısını çal`}>
              {activeSong?.id === song.id && playing ? "♫" : index + 1}
            </button>

            <button className="song-info" onClick={() => onPlaySong(song)}>
              <Cover color={song.color} icon={song.icon} small />
              <span>
                <strong>{song.title}</strong>
                <small>{song.artist}{song.albumName ? ` · ${song.albumName}` : ""}</small>
              </span>
            </button>

            <span className="duration">{song.time}</span>

            <button
              className={`heart-button ${favorites.includes(song.id) ? "is-favorite" : ""}`}
              aria-label="Favorilere ekle veya çıkar"
              disabled={favoriteActionLoading}
              onClick={() => onToggleFavorite(song.id)}
            >
              {favorites.includes(song.id) ? "♥" : "♡"}
            </button>

            {selectedPlaylistData && (
              selectedPlaylistSongIds.includes(song.id) ? (
                <button onClick={() => onRemoveSongFromPlaylist(song)}>Çıkar</button>
              ) : (
                <button onClick={() => onAddSongToPlaylist(song)}>+ Playlist'e ekle</button>
              )
            )}
          </div>
        ))}
      </div>
    </>
  );
}
