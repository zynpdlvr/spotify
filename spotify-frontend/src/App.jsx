import { useEffect, useMemo, useState } from "react";
import "./App.css";

const API_BASE = "http://localhost:8081/api/v1";

const COVER_COLORS = ["purple", "pink", "blue", "orange", "red"];

function Cover({ color = "purple", icon = "♫", small = false }) {
  return (
      <div className={`cover ${color} ${small ? "cover-small" : ""}`}>
        <span>{icon}</span>
      </div>
  );
}

// HATEOAS yanıtındaki gömülü listeyi bulur.
function getEmbeddedList(data) {
  const embedded = data?._embedded ?? {};
  return Object.values(embedded).find(Array.isArray) ?? [];
}

export default function App() {
  const [activeMenu, setActiveMenu] = useState("Ana Sayfa");
  const [search, setSearch] = useState("");
  const [currentSong, setCurrentSong] = useState(null);
  const [playing, setPlaying] = useState(false);
  const [favorites, setFavorites] = useState([]);
  const [activePlaylist, setActivePlaylist] = useState(null);

  // API verileri
  const [songs, setSongs] = useState([]);
  const [albums, setAlbums] = useState([]);
  const [playlists, setPlaylists] = useState([]);

  // Şarkıların yüklenme ve hata durumları
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Albüm yüklenme ve hata durumları
  const [albumsLoading, setAlbumsLoading] = useState(true);
  const [albumsError, setAlbumsError] = useState("");

  // Playlist yüklenme ve hata durumları
  const [playlistsLoading, setPlaylistsLoading] = useState(true);
  const [playlistsError, setPlaylistsError] = useState("");

  const [favoritesLoading, setFavoritesLoading] = useState(false);
  const [favoritesError, setFavoritesError] = useState("");
  const [favoriteActionLoading, setFavoriteActionLoading] = useState(false);


  const [selectedAlbum, setSelectedAlbum] = useState(null);
  const [selectedAlbumSongIds, setSelectedAlbumSongIds] = useState([]);
  const [selectedPlaylistData, setSelectedPlaylistData] = useState(null);
  const [selectedPlaylistSongIds, setSelectedPlaylistSongIds] = useState([]);


  //login
  const [loginOpen, setLoginOpen] = useState(false);
  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [loginLoading, setLoginLoading] = useState(false);
  const [loginError, setLoginError] = useState("");
  const [authVersion, setAuthVersion] = useState(0);
  const [loggedIn, setLoggedIn] = useState(
      Boolean(localStorage.getItem("accessToken"))
  );
  // ŞARKILARI API'DEN GETİR
  useEffect(() => {
    const controller = new AbortController();




    function handleLogout() {
      localStorage.removeItem("accessToken");

      setLoggedIn(false);
      setFavorites([]);
      setPlaylists([]);
      setSelectedPlaylistData(null);
      setSelectedPlaylistSongIds([]);
      setSelectedAlbum(null);
      setSelectedAlbumSongIds([]);
      setActivePlaylist(null);
      setPlaylistsError("");
      setFavoritesError("");
      setLoginPassword("");
    }


    async function fetchSongs() {
      try {
        setLoading(true);
        setError("");

        const response = await fetch(`${API_BASE}/songs`, {
          signal: controller.signal,
        });

        if (!response.ok) {
          throw new Error(`Şarkı API hatası: ${response.status}`);
        }

        const data = await response.json();
        const songList = data?._embedded?.songResponseList ?? [];

        const mappedSongs = songList.map((song, index) => ({
          ...song,
          id: song.publicId,
          color: COVER_COLORS[index % COVER_COLORS.length],
          icon: "♫",
          time: "—",
        }));

        setSongs(mappedSongs);
      } catch (err) {
        if (err.name !== "AbortError") {
          setError(err.message || "Şarkılar yüklenemedi.");
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    fetchSongs();

    return () => controller.abort();
  }, []);


  async function apiRequest(path, options = {}) {
    const token = localStorage.getItem("accessToken");

    const response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers: {
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...options.headers,
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    });

    if (!response.ok) {
      const message =
          response.status === 401
              ? "Oturum süresi dolmuş. Yeniden giriş yapmalısın."
              : response.status === 403
                  ? "Bu işlem için yetkin yok."
                  : `API hatası: ${response.status}`;

      throw new Error(message);
    }

    if (response.status === 204) return null;

    return response.json();
  }
//Favorites

  useEffect(() => {
    const controller = new AbortController();

    async function fetchFavorites() {
      const token = localStorage.getItem("accessToken");

      // Giriş yapılmamışsa API çağrısı yapma.
      if (!token) {
        setFavorites([]);
        setFavoritesError("");
        setFavoritesLoading(false);
        return;
      }

      try {
        setFavoritesLoading(true);
        setFavoritesError("");

        const data = await apiRequest("/me/favorites", {
          signal: controller.signal,
        });

        setFavorites(
            Array.isArray(data)
                ? data.map((song) => song.publicId).filter(Boolean)
                : []
        );
      } catch (err) {
        if (err.name !== "AbortError") {
          setFavoritesError(err.message || "Favoriler yüklenemedi.");
        }
      } finally {
        if (!controller.signal.aborted) {
          setFavoritesLoading(false);
        }
      }
    }

    fetchFavorites();

    return () => controller.abort();
  }, [authVersion]);

  // ALBÜMLERİ API'DEN GETİR
  useEffect(() => {
    const controller = new AbortController();

    async function fetchAlbums() {
      try {
        setAlbumsLoading(true);
        setAlbumsError("");

        const response = await fetch(`${API_BASE}/albums`, {
          signal: controller.signal,
        });

        if (!response.ok) {
          throw new Error(`Albüm API hatası: ${response.status}`);
        }

        const data = await response.json();
        const albumList = getEmbeddedList(data);

        setAlbums(
            albumList.map((album, index) => ({
              ...album,
              id: album.publicId ?? album.id,
              color: COVER_COLORS[index % COVER_COLORS.length],
              icon: "♫",
            }))
        );
      } catch (err) {
        if (err.name !== "AbortError") {
          setAlbumsError(err.message || "Albümler yüklenemedi.");
        }
      } finally {
        if (!controller.signal.aborted) {
          setAlbumsLoading(false);
        }
      }
    }

    fetchAlbums();

    return () => controller.abort();
  }, []);

  // GİRİŞ YAPAN KULLANICININ PLAYLIST'LERİNİ GETİR

  useEffect(() => {
    const controller = new AbortController();

    async function fetchPlaylists() {
      const token = localStorage.getItem("accessToken");

      // Giriş yapılmamışsa API çağrısı yapma.
      if (!token) {
        setPlaylists([]);
        setPlaylistsError("");
        setPlaylistsLoading(false);
        return;
      }

      try {
        setPlaylistsLoading(true);
        setPlaylistsError("");

        const response = await fetch(`${API_BASE}/playlists`, {
          signal: controller.signal,
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        if (!response.ok) {
          if (response.status === 401) {
            throw new Error("Oturum geçersiz veya süresi dolmuş. Tekrar giriş yap.");
          }

          if (response.status === 403) {
            throw new Error("Playlist'lere erişim iznin yok.");
          }

          throw new Error(`Playlist API hatası: ${response.status}`);
        }

        const data = await response.json();
        const playlistList = getEmbeddedList(data);

        setPlaylists(
            playlistList.map((playlist, index) => ({
              ...playlist,
              id: playlist.publicId ?? playlist.id,
              title: playlist.name,
              color: COVER_COLORS[index % COVER_COLORS.length],
              icon: "♫",
            }))
        );
      } catch (err) {
        if (err.name !== "AbortError") {
          setPlaylistsError(err.message || "Playlist'ler yüklenemedi.");
        }
      } finally {
        if (!controller.signal.aborted) {
          setPlaylistsLoading(false);
        }
      }
    }

    fetchPlaylists();

    return () => controller.abort();
  }, [authVersion]);


  // ARAMA: ŞARKI, SANATÇI VE ALBÜM ADI
  const filteredSongs = useMemo(() => {
    const term = search.trim().toLocaleLowerCase("tr");

    return songs.filter((song) =>
        `${song.title ?? ""} ${song.artist ?? ""} ${song.albumName ?? ""}`
            .toLocaleLowerCase("tr")
            .includes(term)
    );
  }, [search, songs]);

  const activeSong = currentSong ?? songs[0] ?? null;

  const visibleSongs = filteredSongs.filter((song) => {
    if (
        activeMenu === "Beğenilen Şarkılar" &&
        !favorites.includes(song.id)
    ) {
      return false;
    }

    if (
        selectedAlbum &&
        !selectedAlbumSongIds.includes(song.id)
    ) {
      return false;
    }

    if (
        selectedPlaylistData &&
        !selectedPlaylistSongIds.includes(song.id)
    ) {
      return false;
    }

    return true;
  });

  function playSong(song) {
    if (!song) return;

    setCurrentSong(song);
    setPlaying(true);
  }

  async function toggleFavorite(songPublicId) {
    if (!songPublicId || favoriteActionLoading) return;

    const wasFavorite = favorites.includes(songPublicId);

    try {
      setFavoriteActionLoading(true);
      setFavoritesError("");

      await apiRequest(
          `/me/favorites/${encodeURIComponent(songPublicId)}`,
          { method: wasFavorite ? "DELETE" : "PUT" }
      );

      setFavorites((previous) =>
          wasFavorite
              ? previous.filter((id) => id !== songPublicId)
              : previous.includes(songPublicId)
                  ? previous
                  : [...previous, songPublicId]
      );
    } catch (err) {
      setFavoritesError(err.message);
    } finally {
      setFavoriteActionLoading(false);
    }
  }

  function playRelativeSong(direction) {
    if (songs.length === 0) return;

    const currentIndex = songs.findIndex(
        (song) => song.id === activeSong?.id
    );

    const startIndex = currentIndex < 0 ? 0 : currentIndex;
    const nextIndex =
        (startIndex + direction + songs.length) % songs.length;

    playSong(songs[nextIndex]);
  }

  // PLAYLIST DETAYINI YENİDEN GETİR
  async function refreshPlaylistDetails(publicId) {
    const detail = await apiRequest(
        `/playlists/${encodeURIComponent(publicId)}`
    );

    setSelectedPlaylistData(detail);
    setSelectedPlaylistSongIds(
        (detail.songs ?? []).map((song) => song.publicId).filter(Boolean)
    );

    return detail;
  }

  // PLAYLIST SEÇ
  async function selectPlaylist(playlist) {
    const publicId = playlist.publicId ?? playlist.id;
    if (!publicId) {
      setPlaylistsError("Playlist kimliği bulunamadı.");
      return;
    }

    try {
      setPlaylistsError("");
      const detail = await refreshPlaylistDetails(publicId);

      setSelectedAlbum(null);
      setSelectedAlbumSongIds([]);
      setActivePlaylist(detail.name ?? playlist.name ?? playlist.title);
      setActiveMenu("Kitaplığın");
      setSearch("");
    } catch (err) {
      setPlaylistsError(err.message || "Playlist detayları yüklenemedi.");
    }
  }

  // ALBÜM SEÇ
  async function selectAlbum(album) {
    const publicId = album.publicId ?? album.id;
    if (!publicId) {
      setError("Albüm kimliği bulunamadı.");
      return;
    }

    try {
      setError("");

      const detail = await apiRequest(
          `/albums/${encodeURIComponent(publicId)}`
      );

      setSelectedAlbum(detail);
      setSelectedAlbumSongIds(
          (detail.songs ?? []).map((song) => song.publicId).filter(Boolean)
      );

      setSelectedPlaylistData(null);
      setSelectedPlaylistSongIds([]);
      setActivePlaylist(null);
      setActiveMenu("Kitaplığın");
      setSearch("");
    } catch (err) {
      setError(`Albüm detayları yüklenemedi: ${err.message}`);
    }
  }

  // PLAYLIST OLUŞTUR
  async function createPlaylist() {
    const name = window.prompt("Playlist adı:");
    if (!name?.trim()) return;

    const description = window.prompt("Playlist açıklaması:") ?? "";

    try {
      setPlaylistsError("");

      const created = await apiRequest("/playlists", {
        method: "POST",
        body: JSON.stringify({
          name: name.trim(),
          description: description.trim(),
        }),
      });

      const playlist = {
        ...created,
        id: created.publicId,
        title: created.name,
        color: COVER_COLORS[playlists.length % COVER_COLORS.length],
        icon: "♫",
      };

      setPlaylists((previous) => [...previous, playlist]);
      await selectPlaylist(playlist);
    } catch (err) {
      setPlaylistsError(err.message || "Playlist oluşturulamadı.");
    }
  }

  // PLAYLIST GÜNCELLE
  async function updatePlaylist() {
    if (!selectedPlaylistData?.publicId) return;

    const name = window.prompt(
        "Yeni playlist adı:",
        selectedPlaylistData.name
    );
    if (!name?.trim()) return;

    const description =
        window.prompt(
            "Yeni açıklama:",
            selectedPlaylistData.description ?? ""
        ) ?? "";

    try {
      setPlaylistsError("");

      await apiRequest(
          `/playlists/${encodeURIComponent(selectedPlaylistData.publicId)}`,
          {
            method: "PUT",
            body: JSON.stringify({
              name: name.trim(),
              description: description.trim(),
            }),
          }
      );

      const updated = await refreshPlaylistDetails(
          selectedPlaylistData.publicId
      );

      setActivePlaylist(updated.name);

      setPlaylists((previous) =>
          previous.map((item) =>
              item.publicId === updated.publicId
                  ? { ...item, ...updated, id: updated.publicId, title: updated.name }
                  : item
          )
      );
    } catch (err) {
      setPlaylistsError(err.message || "Playlist güncellenemedi.");
    }
  }

  // PLAYLIST SİL
  async function deletePlaylist() {
    if (!selectedPlaylistData?.publicId) return;

    const confirmed = window.confirm(
        `"${selectedPlaylistData.name}" playlist'i silinsin mi?`
    );
    if (!confirmed) return;

    try {
      setPlaylistsError("");

      const publicId = selectedPlaylistData.publicId;

      await apiRequest(
          `/playlists/${encodeURIComponent(publicId)}`,
          { method: "DELETE" }
      );

      setPlaylists((previous) =>
          previous.filter((item) => item.publicId !== publicId)
      );

      setSelectedPlaylistData(null);
      setSelectedPlaylistSongIds([]);
      setActivePlaylist(null);
    } catch (err) {
      setPlaylistsError(err.message || "Playlist silinemedi.");
    }
  }

  // PLAYLIST'E ŞARKI EKLE
  async function addSongToPlaylist(song) {
    const playlistId = selectedPlaylistData?.publicId;
    const songId = song.publicId ?? song.id;

    if (!playlistId || !songId) return;

    try {
      setPlaylistsError("");

      await apiRequest(
          `/playlists/${encodeURIComponent(playlistId)}/songs/${encodeURIComponent(songId)}`,
          { method: "PUT" }
      );

      await refreshPlaylistDetails(playlistId);
    } catch (err) {
      setPlaylistsError(err.message || "Şarkı playlist'e eklenemedi.");
    }
  }

  // PLAYLIST'TEN ŞARKI ÇIKAR
  async function removeSongFromPlaylist(song) {
    const playlistId = selectedPlaylistData?.publicId;
    const songId = song.publicId ?? song.id;

    if (!playlistId || !songId) return;

    try {
      setPlaylistsError("");

      await apiRequest(
          `/playlists/${encodeURIComponent(playlistId)}/songs/${encodeURIComponent(songId)}`,
          { method: "DELETE" }
      );

      await refreshPlaylistDetails(playlistId);
    } catch (err) {
      setPlaylistsError(err.message || "Şarkı playlist'ten çıkarılamadı.");
    }


  }

  function handleLibraryClick() {
    const token = localStorage.getItem("accessToken");

    if (!token) {
      setLoginError("");
      setLoginOpen(true);
      return;
    }

    setActiveMenu("Kitaplığın");
    setSelectedAlbum(null);
    setSelectedAlbumSongIds([]);
    setSelectedPlaylistData(null);
    setSelectedPlaylistSongIds([]);
  }

  async function handleLogin(event) {
    event.preventDefault();

    setLoginLoading(true);
    setLoginError("");

    try {
      const response = await fetch(`${API_BASE}/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          email: loginEmail.trim(),
          password: loginPassword,
        }),
      });

      const data = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(
            data.message ||
            data.detail ||
            `Giriş başarısız: ${response.status}`
        );
      }

      const token = data.accessToken ?? data.token;

      if (!token) {
        throw new Error("Giriş yanıtında token bulunamadı.");
      }

      localStorage.setItem("accessToken", token);
      setLoggedIn(true);
      setAuthVersion((version) => version + 1);
      setLoginOpen(false);
      setLoginPassword("");
      setLoginError("");
      setActiveMenu("Kitaplığın");
    } catch (err) {
      setLoginError(err.message || "Giriş sırasında hata oluştu.");
    } finally {
      setLoginLoading(false);
    }
  }


  return (
      <div className="app-shell">
        {/* SOL MENÜ */}
        <aside className="sidebar">
          <a
              className="brand"
              href="#home"
              onClick={(event) => {
                event.preventDefault();
                setActiveMenu("Ana Sayfa");
                setActivePlaylist(null);
                setSelectedAlbum(null);
                setSelectedAlbumSongIds([]);
                setSelectedPlaylistData(null);
                setSelectedPlaylistSongIds([]);
                setSearch("");
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
                    className={`nav-item ${activeMenu === label ? "selected" : ""}`}
                    onClick={() => {
                      setActiveMenu(label);
                      setActivePlaylist(null);
                      setSelectedAlbum(null);
                      setSelectedAlbumSongIds([]);
                      setSelectedPlaylistData(null);
                      setSelectedPlaylistSongIds([]);
                    }}
                >
                  <span className="nav-icon">{icon}</span>
                  {label}
                </button>
            ))}
          </nav>

          <p className="section-label">ÇALMA LİSTELERİN</p>

          <button className="nav-item" onClick={createPlaylist}>
            <span className="nav-icon">＋</span>
            Çalma listesi oluştur
          </button>

          <button
              className={`nav-item ${
                  activeMenu === "Beğenilen Şarkılar" ? "selected" : ""
              }`}
              onClick={() => {
                setActiveMenu("Beğenilen Şarkılar");
                setActivePlaylist(null);
                setSelectedAlbum(null);
                setSelectedAlbumSongIds([]);
                setSelectedPlaylistData(null);
                setSelectedPlaylistSongIds([]);
                setSearch("");
              }}
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
                      className="nav-item playlist-link"
                      key={playlist.publicId ?? playlist.id}
                      onClick={() => selectPlaylist(playlist)}
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

        {/* ANA İÇERİK */}
        <main className="main-content">
          <header className="topbar">
            <div className="search-box">
              <span>⌕</span>
              <input
                  aria-label="Şarkı, sanatçı veya albüm ara"
                  placeholder="Şarkı, sanatçı veya albüm ara..."
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
              />

              {search && (
                  <button
                      className="clear-search"
                      onClick={() => setSearch("")}
                      aria-label="Aramayı temizle"
                  >
                    ×
                  </button>
              )}
            </div>


            <div className="profile-menu">
              <button
                  className="profile-button"
                  onClick={() => {
                    if (loggedIn) {
                      handleLogout();
                    } else {
                      setLoginError("");
                      setLoginOpen(true);
                    }
                  }}
              >
    <span className="profile-avatar">
      {loggedIn ? "✓" : "Z"}
    </span>
                <span>{loggedIn ? "Çıkış yap" : "Giriş yap"}</span>
              </button>
            </div>

          </header>

          <section className="content-section">
            <div className="greeting">
              <p className="eyebrow">MÜZİK SENİNLE</p>
              <h1>
                {search
                    ? "Arama sonuçları"
                    : activeMenu === "Beğenilen Şarkılar"
                        ? "Beğenilen Şarkılar"
                        : selectedAlbum
                            ? selectedAlbum.name
                            : selectedPlaylistData
                                ? selectedPlaylistData.name
                                : "Tekrar hoş geldin"}
              </h1>
              <p>Bugünün müziğini keşfet.</p>
            </div>

            {/* ANA SAYFA */}
            {!search &&
                activeMenu === "Ana Sayfa" &&
                !selectedAlbum &&
                !selectedPlaylistData && (
                    <>
                      <h2>Senin için</h2>

                      {playlistsLoading && (
                          <p className="empty-state">Playlist'ler yükleniyor...</p>
                      )}

                      <div className="playlist-grid">
                        {!playlistsLoading &&
                            playlists.map((playlist) => (
                                <button
                                    className="playlist-card"
                                    key={playlist.publicId ?? playlist.id}
                                    onClick={() => selectPlaylist(playlist)}
                                >
                                  <Cover
                                      color={playlist.color}
                                      icon={playlist.icon}
                                  />
                                  <span className="card-title">
                        {playlist.title ?? playlist.name}
                      </span>
                                  <span className="card-description">
                        {playlist.description ?? "Çalma listesi"}
                      </span>
                                  <span className="play-float">▶</span>
                                </button>
                            ))}
                      </div>

                      {/* ALBÜMLER */}
                      <div className="section-heading">
                        <h2>Albümler</h2>
                        <span className="muted">{albums.length} albüm</span>
                      </div>

                      {albumsLoading && (
                          <p className="empty-state">Albümler yükleniyor...</p>
                      )}

                      {albumsError && (
                          <p className="empty-state" role="alert">{albumsError}</p>
                      )}

                      {!albumsLoading && albums.length === 0 && (
                          <p className="empty-state">Henüz albüm bulunamadı.</p>
                      )}

                      <div className="recent-grid">
                        {!albumsLoading &&
                            albums.map((album) => (
                                <button
                                    className="recent-card"
                                    key={album.publicId ?? album.id}
                                    onClick={() => selectAlbum(album)}
                                >
                                  <Cover color={album.color} icon={album.icon} />
                                  <strong>{album.name ?? "İsimsiz albüm"}</strong>
                                  <span>{album.artistName ?? "Bilinmeyen sanatçı"}</span>
                                </button>
                            ))}
                      </div>

                      {/* SON EKLENEN ŞARKILAR */}
                      <div className="section-heading">
                        <h2>Şarkılar</h2>
                        <span className="muted">{songs.length} şarkı</span>
                      </div>

                      <div className="recent-grid">
                        {songs.slice(0, 4).map((song) => (
                            <button
                                className="recent-card"
                                key={song.id}
                                onClick={() => playSong(song)}
                            >
                              <Cover color={song.color} icon={song.icon} />
                              <strong>{song.title}</strong>
                              <span>{song.artist}</span>
                            </button>
                        ))}
                      </div>
                    </>
                )}

            {/* SEÇİLİ PLAYLIST İŞLEMLERİ */}
            {selectedPlaylistData && (
                <div className="section-heading">
                  <div>
                    <h2>{selectedPlaylistData.name}</h2>
                    <p>
                      {selectedPlaylistData.description ??
                          "Playlist açıklaması yok."}
                    </p>
                  </div>

                  <div className="playlist-actions">
                    <button onClick={updatePlaylist}>Düzenle</button>
                    <button onClick={deletePlaylist}>Playlist'i sil</button>
                  </div>
                </div>
            )}

            {/* HATA MESAJLARI */}
            {favoritesError && (
                <p className="empty-state" role="alert">{favoritesError}</p>
            )}

            {playlistsError && (
                <p className="empty-state" role="alert">{playlistsError}</p>
            )}

            {/* ŞARKI LİSTESİ */}
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

              <span className="muted">{visibleSongs.length} şarkı</span>
            </div>

            <div className="song-table">
              <div className="song-table-header">
                <span>#</span>
                <span>Şarkı</span>
                <span className="duration">Süre</span>
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

              {!loading &&
                  !error &&
                  visibleSongs.map((song, index) => (
                      <div
                          className={`song-row ${
                              activeSong?.id === song.id ? "currently-selected" : ""
                          }`}
                          key={song.id}
                      >
                        <button
                            className="row-number"
                            onClick={() => playSong(song)}
                            aria-label={`${song.title} şarkısını çal`}
                        >
                          {activeSong?.id === song.id && playing
                              ? "♫"
                              : index + 1}
                        </button>

                        <button
                            className="song-info"
                            onClick={() => playSong(song)}
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

                        {selectedPlaylistData && (
                            selectedPlaylistSongIds.includes(song.id) ? (
                                <button
                                    onClick={() => removeSongFromPlaylist(song)}
                                >
                                  Çıkar
                                </button>
                            ) : (
                                <button
                                    onClick={() => addSongToPlaylist(song)}
                                >
                                  + Playlist'e ekle
                                </button>
                            )
                        )}
                      </div>
                  ))}
            </div>
          </section>
        </main>


        {loginOpen && (
            <div
                className="login-overlay"
                onMouseDown={(event) => {
                  if (event.target === event.currentTarget) {
                    setLoginOpen(false);
                  }
                }}
            >
              <section
                  className="login-modal"
                  role="dialog"
                  aria-modal="true"
                  aria-labelledby="login-title"
              >
                <button
                    className="login-close"
                    type="button"
                    onClick={() => setLoginOpen(false)}
                    aria-label="Giriş penceresini kapat"
                >
                  ×
                </button>

                <div className="login-brand">♫ spotify</div>

                <h2 id="login-title">Tekrar hoş geldin</h2>
                <p className="login-subtitle">
                  Hesabına giriş yap ve müziğine devam et.
                </p>

                <form onSubmit={handleLogin}>
                  <label htmlFor="login-email">E-posta</label>
                  <input
                      id="login-email"
                      type="email"
                      autoComplete="email"
                      placeholder="ornek@email.com"
                      value={loginEmail}
                      onChange={(event) => setLoginEmail(event.target.value)}
                      required
                  />

                  <label htmlFor="login-password">Şifre</label>
                  <input
                      id="login-password"
                      type="password"
                      autoComplete="current-password"
                      placeholder="Şifreni gir"
                      value={loginPassword}
                      onChange={(event) => setLoginPassword(event.target.value)}
                      required
                  />

                  {loginError && (
                      <p className="login-error" role="alert">
                        {loginError}
                      </p>
                  )}

                  <button
                      className="login-submit"
                      type="submit"
                      disabled={loginLoading}
                  >
                    {loginLoading ? "Giriş yapılıyor..." : "Giriş yap"}
                  </button>
                </form>

                <p className="login-footer">
                  Henüz hesabın yok mu? Önce mevcut kayıt olma endpoint'ini kullanabilirsin.
                </p>
              </section>
            </div>
        )}


        {/* ALT MÜZİK ÇALAR */}
        <footer className="player">
          <div className="now-playing">
            {activeSong ? (
                <>
                  <Cover
                      color={activeSong.color}
                      icon={activeSong.icon}
                      small
                  />

                  <div>
                    <strong>{activeSong.title}</strong>
                    <small>{activeSong.artist}</small>
                  </div>

                  <button
                      className={`heart-button ${
                          favorites.includes(activeSong.id) ? "is-favorite" : ""
                      }`}
                      disabled={favoriteActionLoading}
                      onClick={() => toggleFavorite(activeSong.id)}
                      aria-label="Favorilere ekle veya çıkar"
                  >
                    {favorites.includes(activeSong.id) ? "♥" : "♡"}
                  </button>
                </>
            ) : (
                <span>Henüz şarkı seçilmedi</span>
            )}
          </div>

          <div className="player-center">
            <div className="player-controls">
              <button
                  aria-label="Önceki şarkı"
                  disabled={songs.length === 0}
                  onClick={() => playRelativeSong(-1)}
              >
                |◀
              </button>

              <button
                  className="play-button"
                  aria-label={playing ? "Duraklat" : "Oynat"}
                  disabled={!activeSong}
                  onClick={() => setPlaying((value) => !value)}
              >
                {playing ? "Ⅱ" : "▶"}
              </button>

              <button
                  aria-label="Sonraki şarkı"
                  disabled={songs.length === 0}
                  onClick={() => playRelativeSong(1)}
              >
                ▶|
              </button>
            </div>

            <div className="progress-row">
              <span>0:00</span>
              <div className="progress-track">
                <div />
              </div>
              <span>{activeSong?.time ?? "—"}</span>
            </div>
          </div>

          <div className="player-extra">
            <span>♫</span>
            <div className="volume-track">
              <div />
            </div>
          </div>
        </footer>

        {/* BİLDİRİM */}
        {activePlaylist && (
            <div className="toast" role="status">
              {`${activePlaylist} seçildi.`}
              <button
                  onClick={() => setActivePlaylist(null)}
                  aria-label="Bildirimi kapat"
              >
                ×
              </button>
            </div>
        )}
      </div>
  );
}