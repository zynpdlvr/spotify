import Cover from "./Cover";

export default function Sidebar({
  activeMenu,
  playlists,
  playlistsLoading,
  playlistsError,
  songLoading,
  songError,
  onNavigate,
  onCreatePlaylist,
  onSelectPlaylist,
  onClearPlaylistError,
}) {
  return (
    <aside className="sidebar">
      <a className="brand" href="#home" onClick={(event) => {
        event.preventDefault();
        onNavigate("Ana Sayfa");
      }}>
        <span className="brand-icon">≋</span>
        <span>spotify</span>
      </a>

      <nav className="main-nav">
        {[
          ["⌂", "Ana Sayfa"],
          ["⌕", "Ara"],
          ["▤", "Kitaplığın"],
        ].map(([icon, label]) => (
          <button
            key={label}
            className={`nav-item ${activeMenu === label ? "selected" : ""}`}
            onClick={() => onNavigate(label)}
          >
            <span className="nav-icon">{icon}</span>
            {label}
          </button>
        ))}
      </nav>

      <p className="section-label">ÇALMA LİSTELERİN</p>

      <button className="nav-item" onClick={onCreatePlaylist}>
        <span className="nav-icon">＋</span>
        Çalma listesi oluştur
      </button>

      <button
        className={`nav-item ${activeMenu === "Beğenilen Şarkılar" ? "selected" : ""}`}
        onClick={() => onNavigate("Beğenilen Şarkılar")}
      >
        <span className="liked-icon">♥</span>
        Beğenilen Şarkılar
      </button>

      {playlistsLoading && (
        <p className="empty-state">Playlist'ler yükleniyor...</p>
      )}

      {playlistsError && (
        <p className="empty-state" role="alert">
          {playlistsError}
          <button type="button" onClick={onClearPlaylistError} aria-label="Hata mesajını kapat">
            ×
          </button>
        </p>
      )}

      {!playlistsLoading &&
        playlists.map((playlist) => (
          <button
            className="nav-item playlist-link"
            key={playlist.publicId ?? playlist.id}
            onClick={() => onSelectPlaylist(playlist)}
          >
            <Cover color={playlist.color} icon={playlist.icon} small />
            {playlist.title ?? playlist.name ?? "İsimsiz playlist"}
          </button>
        ))}

      <div className="sidebar-bottom">
        <span className="online-dot" />
        {songError
          ? "Şarkı API bağlantısı kontrol edilmeli"
          : songLoading
            ? "Backend'e bağlanılıyor..."
            : "Şarkılar yüklendi"}
      </div>
    </aside>
  );
}
