import Cover from "./Cover";
import AddToPlaylistDropdown from "./AddToPlaylistDropdown";

export default function SongTable({
  visibleSongs,
  loading,
  error,
  favoritesLoading,
  favorites,
  favoriteActionLoading,
  activeMenu,
  selectedPlaylistData,
  selectedPlaylistSongIds,
  search,
  toggleFavorite,
  addSongToPlaylist,
  removeSongFromPlaylist,
  playlists,
  playlistsLoading,
  onPromptLogin,
}) {
  const emptyMessage = activeMenu === "Beğenilen Şarkılar"
      ? "Henüz favori şarkın yok."
      : selectedPlaylistData
          ? "Bu playlist'te henüz şarkı yok."
          : search
              ? "Bu aramayla eşleşen şarkı bulunamadı."
              : "Henüz şarkı bulunamadı.";

  return (
      <div className="song-table">
        <div className="song-table-header">
          <span>#</span>
          <span>Şarkı</span>
          <span className="duration">Süre</span>
          <span />
          <span />
          <span />
        </div>

        {loading && (
            <p className="empty-state">Şarkılar yükleniyor...</p>
        )}

        {!loading && error && (
            <p className="empty-state" role="alert">
              Şarkılar yüklenemedi: {error}
            </p>
        )}

        {!loading && !error && favoritesLoading &&
            activeMenu === "Beğenilen Şarkılar" && (
                <p className="empty-state">Favoriler yükleniyor...</p>
            )}

        {!loading && !error && visibleSongs.length === 0 && (
            <p className="empty-state">
              {emptyMessage}
            </p>
        )}

        {!loading &&
            !error &&
            visibleSongs.map((song, index) => (
                <div
                    className={`song-row ${
                        activeMenu === "Beğenilen Şarkılar" &&
                        favorites.includes(song.id)
                            ? "currently-selected"
                            : ""
                    }`}
                    key={song.id}
                >
                  <button
                      className="row-number"
                      onClick={() => {}}
                      aria-label={`${song.title} şarkısını çal`}
                  >
                    {index + 1}
                  </button>

                  <button
                      className="song-info"
                      onClick={() => {}}
                  >
                    <Cover color={song.color} icon={song.icon} small />
                    <span>
                      <strong>{song.title}</strong>
                      <small>
                        {song.artist}
                        {song.albumName ? ` · ${song.albumName}` : ""}
                      </small>
                    </span>
                  </button>

                  <span className="duration">{song.time}</span>

                  <button
                      className={`heart-button ${
                          favorites.includes(song.id) ? "is-favorite" : ""
                      }`}
                      aria-label="Favorilere ekle veya çıkar"
                      disabled={favoriteActionLoading}
                      onClick={() => toggleFavorite(song.id)}
                  >
                    {favorites.includes(song.id) ? "♥" : "♡"}
                  </button>

                  {/* "Add to playlist" dropdown — always visible */}
                  <AddToPlaylistDropdown
                      song={song}
                      playlists={playlists ?? []}
                      playlistsLoading={playlistsLoading}
                      onAdd={addSongToPlaylist}
                      onPromptLogin={onPromptLogin}
                  />

                  {/* Remove from current playlist button — only when viewing a playlist */}
                  {selectedPlaylistData && selectedPlaylistSongIds.includes(song.id) && (
                      <button
                          className="remove-from-playlist-btn"
                          onClick={() => removeSongFromPlaylist(song)}
                          title="Bu playlist'ten çıkar"
                      >
                        Çıkar
                      </button>
                  )}
                </div>
            ))}
      </div>
  );
}
