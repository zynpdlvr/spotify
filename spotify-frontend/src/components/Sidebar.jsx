
import Cover from "./Cover";

export default function Sidebar({
                                    activeMenu,
                                    playlists = [],
                                    playlistsLoading,
                                    playlistsError,
                                    setPlaylistsError,
                                    loading,
                                    error,
                                    onNavigate,
                                    onCreatePlaylist,
                                    onSelectPlaylist,
                                    onSelectLikedSongs,
                                    canCreateCatalog = false,
                                    onCreateAlbum,
                                    onCreateSong,
                                }) {
    return (
        <aside className="sidebar">
            <a
                className="brand"
                href="#home"
                onClick={(event) => {
                    event.preventDefault();
                    onNavigate("Ana Sayfa");
                }}
            >
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
                        type="button"
                        className={`nav-item ${
                            activeMenu === label ? "selected" : ""
                        }`}
                        onClick={() => onNavigate(label)}
                    >
                        <span className="nav-icon">{icon}</span>
                        {label}
                    </button>
                ))}
            </nav>

            <p className="section-label">ÇALMA LİSTELERİN</p>

            <button
                type="button"
                className="nav-item"
                onClick={onCreatePlaylist}
            >
                <span className="nav-icon">＋</span>
                Çalma listesi oluştur
            </button>

            {canCreateCatalog && (
                <>
                    <p className="section-label">İÇERİK YÖNETİMİ</p>

                    <button
                        type="button"
                        className={`nav-item ${
                            activeMenu === "Albüm Oluştur" ? "selected" : ""
                        }`}
                        onClick={onCreateAlbum}
                    >
                        <span className="nav-icon">▣</span>
                        Albüm oluştur
                    </button>

                    <button
                        type="button"
                        className={`nav-item ${
                            activeMenu === "Şarkı Oluştur" ? "selected" : ""
                        }`}
                        onClick={onCreateSong}
                    >
                        <span className="nav-icon">♫</span>
                        Şarkı oluştur
                    </button>
                </>
            )}

            <button
                type="button"
                className={`nav-item ${
                    activeMenu === "Beğenilen Şarkılar" ? "selected" : ""
                }`}
                onClick={onSelectLikedSongs}
            >
                <span className="liked-icon">♥</span>
                Beğenilen Şarkılar
            </button>

            {playlistsLoading && (
                <p className="empty-state">
                    Playlist'ler yükleniyor...
                </p>
            )}

            {playlistsError && (
                <p className="empty-state" role="alert">
                    {playlistsError}
                    <button
                        type="button"
                        onClick={() => setPlaylistsError("")}
                        aria-label="Hata mesajını kapat"
                    >
                        ×
                    </button>
                </p>
            )}

            {!playlistsLoading &&
                playlists.map((playlist) => (
                    <button
                        type="button"
                        className={`nav-item playlist-link ${
                            activeMenu === playlist.title ||
                            activeMenu === playlist.name
                                ? "selected"
                                : ""
                        }`}
                        key={playlist.publicId ?? playlist.id}
                        onClick={() => onSelectPlaylist(playlist)}
                    >
                        <Cover
                            color={playlist.color}
                            icon={playlist.icon}
                            small
                        />
                        {playlist.title ?? playlist.name ?? "İsimsiz playlist"}
                    </button>
                ))}

            <div className="sidebar-bottom">
                <span className="online-dot" />
                {error
                    ? "Şarkı API bağlantısı kontrol edilmeli"
                    : loading
                        ? "Backend'e bağlanılıyor..."
                        : "Şarkılar yüklendi"}
            </div>
        </aside>
    );
}
