import { useMemo, useState } from "react";
import "./App.css";

// API
import { getAlbumById } from "./api/AlbumApi.js";
import { loginRequest, registerRequest } from "./api/authApi";
import { apiRequest } from "./api/client";
import { addSongToPlaylist as addSongToPlaylistApi } from "./api/playlistApi";
import { COVER_COLORS, isArtist } from "./utils/constants";

// HOOKS
import { useSongs } from "./hooks/useSongs";
import { useAlbums } from "./hooks/useAlbums";
import { usePlaylists } from "./hooks/usePlaylists";
import { useFavorites } from "./hooks/useFavorites";
import { useVisibleSongs } from "./hooks/useVisibleSongs";
import { useAuth } from "./hooks/useAuth";
import { useUser } from "./hooks/useUser";

// COMPONENTS
import Cover from "./components/Cover";
import AlbumGrid from "./components/AlbumGrid";
import Sidebar from "./components/Sidebar";
import Topbar from "./components/Topbar";
import LoginModal from "./components/LoginModal";
import SongTable from "./components/SongTable";
import ProfileModal from "./components/ProfileModal";
import AlbumCreatePage from "./pages/AlbumCreatePage";
import SongCreatePage from "./pages/SongCreatePage";

export default function App() {
  const [activeMenu, setActiveMenu] = useState("Ana Sayfa");
  const [search, setSearch] = useState("");
  const [catalogVersion, setCatalogVersion] = useState(0);
  const [activePlaylist, setActivePlaylist] = useState(null);
  const [selectedAlbum, setSelectedAlbum] = useState(null);
  const [selectedAlbumSongIds, setSelectedAlbumSongIds] = useState([]);
  const [selectedPlaylistData, setSelectedPlaylistData] = useState(null);
  const [selectedPlaylistSongIds, setSelectedPlaylistSongIds] = useState([]);

  // Login/Register state
  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [loginLoading, setLoginLoading] = useState(false);
  const [loginError, setLoginError] = useState("");

  // Profile modal state
  const [profileOpen, setProfileOpen] = useState(false);

  // Auth
  const {
    loggedIn,
    user,
    setUser,
    role,
    authVersion,
    loginOpen,
    setLoginOpen,
    handleLoginSuccess,
    handleLogout,
  } = useAuth();

  // User profile hook
  const {
    loading: userLoading,
    error: userError,
    setError: setUserError,
    updateProfile,
    deleteProfile,
  } = useUser({
    loggedIn,
    onUserUpdated: setUser,
    onLogout: () => {
      handleLogout();
      setProfileOpen(false);
    },
  });

  // Albums
  const {
    albums,
    albumsLoading,
    albumsError,
  } = useAlbums(authVersion + catalogVersion);

  // Songs — server-side search
  const { songs, loading, error } = useSongs(search || undefined, catalogVersion);

  // Playlists
  const {
    playlists,
    setPlaylists,
    playlistsLoading,
    playlistsError,
    setPlaylistsError,
  } = usePlaylists(authVersion);

  // Favorites
  const {
    favorites,
    favoritesLoading,
    favoritesError,
    favoriteActionLoading,
    toggleFavorite,
  } = useFavorites(authVersion);

  // With server-side search, songs are already filtered. For album/playlist
  // views we still need to filter locally against the full set.
  const allSongs = useMemo(() => songs, [songs]);

  const visibleSongs = useVisibleSongs({
    filteredSongs: allSongs,
    activeMenu,
    favorites,
    selectedAlbum,
    selectedAlbumSongIds,
    selectedPlaylistData,
    selectedPlaylistSongIds,
  });

  // ---- PLAYLIST HELPERS ----

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

  async function selectPlaylist(playlist) {
    const publicId = playlist.publicId ?? playlist.id;
    if (!publicId) {
      setPlaylistsError("Playlist kimliği bulunamadı.");
      return;
    }

    if (!loggedIn) {
      setLoginOpen(true);
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

  // ---- ALBUM SELECT ----

  async function selectAlbum(album) {
    const publicId = album?.publicId ?? album?.id;

    if (!publicId) {
      console.error("Albüm kimliği bulunamadı:", album);
      return;
    }

    try {
      const detail = await getAlbumById(publicId);

      setSelectedAlbum(detail);

      setSelectedAlbumSongIds(
          (detail.songs ?? [])
              .map((song) => song.publicId ?? song.id)
              .filter((id) => id != null)
      );

      setSelectedPlaylistData(null);
      setSelectedPlaylistSongIds([]);
      setActivePlaylist(null);

      setSearch("");
    } catch (err) {
      console.error("Albüm detayları yüklenemedi:", err);
    }
  }

  // ---- PLAYLIST CRUD ----

  async function createPlaylist() {
    if (!loggedIn) {
      setLoginOpen(true);
      return;
    }

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

  // ---- PLAYLIST SONGS ----

  // Called from AddToPlaylistDropdown — adds song to a specific playlist
  async function addSongToPlaylist(playlistId, songId) {
    if (!loggedIn) {
      throw new Error("Oturum süresi dolmuş. Yeniden giriş yapmalısın.");
    }

    if (!playlistId || !songId) {
      throw new Error("Geçersiz playlist veya şarkı kimliği.");
    }

    setPlaylistsError("");

    const result = await addSongToPlaylistApi(playlistId, songId);

    // If we are currently viewing the playlist that was updated, refresh it
    if (selectedPlaylistData?.publicId === playlistId) {
      await refreshPlaylistDetails(playlistId);
    }

    return result;
  }

  // Called from SongTable's "Çıkar" button — removes song from the currently viewed playlist
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

  // ---- AUTH ACTIONS ----

  async function handleLogin(event) {
    event.preventDefault();

    setLoginLoading(true);
    setLoginError("");

    try {
      const response = await loginRequest(loginEmail.trim(), loginPassword);
      handleLoginSuccess(response);
      setLoginPassword("");
      setLoginError("");
      setActiveMenu("Kitaplığın");
    } catch (err) {
      setLoginError(err.message || "Giriş sırasında hata oluştu.");
    } finally {
      setLoginLoading(false);
    }
  }

  async function handleRegister(displayName, email, password) {
    const response = await registerRequest(displayName, email, password);
    handleLoginSuccess(response);
    setActiveMenu("Kitaplığın");
  }

  function handleNavigate(label) {
    setActiveMenu(label);
    setActivePlaylist(null);
    setSelectedAlbum(null);
    setSelectedAlbumSongIds([]);
    setSelectedPlaylistData(null);
    setSelectedPlaylistSongIds([]);
    setSearch("");
  }

  function handleAuthAction() {
    if (loggedIn) {
      handleLogout();
      setActiveMenu("Ana Sayfa");
      setSelectedAlbum(null);
      setSelectedAlbumSongIds([]);
      setSelectedPlaylistData(null);
      setSelectedPlaylistSongIds([]);
    } else {
      setLoginError("");
      setLoginOpen(true);
    }
  }

  function handleProfileClick() {
    setProfileOpen(true);
    setUserError("");
  }

  // ---- FAVORITE GUARD ----

  async function guardedToggleFavorite(songId) {
    if (!loggedIn) {
      setLoginOpen(true);
      return;
    }
    await toggleFavorite(songId);
  }

  return (
      <div className="app-shell">
        {/* SOL MENÜ */}

        <Sidebar
            activeMenu={activeMenu}
            playlists={playlists}
            playlistsLoading={playlistsLoading}
            playlistsError={playlistsError}
            setPlaylistsError={setPlaylistsError}
            loading={loading}
            error={error}
            onNavigate={handleNavigate}
            onCreatePlaylist={createPlaylist}
            onSelectPlaylist={selectPlaylist}
            onSelectLikedSongs={() => {
              if (!loggedIn) {
                setLoginOpen(true);
                return;
              }

              handleNavigate("Beğenilen Şarkılar");
            }}
            canCreateCatalog={loggedIn && isArtist(role)}
            onCreateAlbum={() => {
              if (!loggedIn) {
                setLoginOpen(true);
                return;
              }

              if (!isArtist(role)) {
                return;
              }

              handleNavigate("Albüm Oluştur");
            }}
            onCreateSong={() => {
              if (!loggedIn) {
                setLoginOpen(true);
                return;
              }

              if (!isArtist(role)) {
                return;
              }

              handleNavigate("Şarkı Oluştur");
            }}
        />


        {/* ANA İÇERİK */}
        <main className="main-content">
          <Topbar
              search={search}
              setSearch={setSearch}
              loggedIn={loggedIn}
              user={user}
              role={role}
              onAuthAction={handleAuthAction}
              onProfileClick={handleProfileClick}
          />

          {activeMenu === "Albüm Oluştur" ? (
              <AlbumCreatePage
                  onCancel={() => handleNavigate("Ana Sayfa")}
                  onCreated={() => setCatalogVersion((version) => version + 1)}
              />
          ) : activeMenu === "Şarkı Oluştur" ? (
              <SongCreatePage
                  albums={albums}
                  onCancel={() => handleNavigate("Ana Sayfa")}
                  onCreated={() => setCatalogVersion((version) => version + 1)}
              />
          ) : (
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

                <>
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
                            <AlbumGrid
                                albums={albums}
                                albumsLoading={albumsLoading}
                                albumsError={albumsError}
                                onSelectAlbum={selectAlbum}
                            />

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

                  <SongTable
                      visibleSongs={visibleSongs}
                      loading={loading}
                      error={error}
                      favoritesLoading={favoritesLoading}
                      favorites={favorites}
                      favoriteActionLoading={favoriteActionLoading}
                      activeMenu={activeMenu}
                      selectedPlaylistData={selectedPlaylistData}
                      selectedPlaylistSongIds={selectedPlaylistSongIds}
                      search={search}
                      toggleFavorite={guardedToggleFavorite}
                      addSongToPlaylist={addSongToPlaylist}
                      removeSongFromPlaylist={removeSongFromPlaylist}
                      playlists={playlists}
                      playlistsLoading={playlistsLoading}
                      onPromptLogin={() => setLoginOpen(true)}
                  />
                </>
              </section>
          )}
        </main>

        {/* GİRİŞ / KAYIT MODALI */}
        <LoginModal
            loginOpen={loginOpen}
            setLoginOpen={setLoginOpen}
            loginEmail={loginEmail}
            setLoginEmail={setLoginEmail}
            loginPassword={loginPassword}
            setLoginPassword={setLoginPassword}
            loginLoading={loginLoading}
            loginError={loginError}
            handleLogin={handleLogin}
            handleRegister={handleRegister}
        />

        {/* PROFİL MODALI */}
        {profileOpen && (
            <ProfileModal
                key={user?.publicId ?? "no-user"}
                profileOpen={profileOpen}
                setProfileOpen={setProfileOpen}
                user={user}
                role={role}
                loading={userLoading}
                error={userError}
                handleUpdate={async (data) => {
                  await updateProfile(data);
                  setProfileOpen(false);
                }}
                handleDelete={async () => {
                  await deleteProfile();
                }}
            />
        )}

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
