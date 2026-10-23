import { useState, useRef, useEffect } from "react";

export default function AddToPlaylistDropdown({
  song,
  playlists,
  playlistsLoading,
  onAdd,
  onPromptLogin,
  className,
}) {
  const [open, setOpen] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const ref = useRef(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event) {
      if (ref.current && !ref.current.contains(event.target)) {
        setOpen(false);
        setError("");
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  function handleToggle() {
    setError("");
    setOpen((prev) => !prev);
  }

  async function handleSelect(playlist) {
    if (!song || !playlist) return;

    const playlistId = playlist.publicId ?? playlist.id;
    const songId = song.publicId ?? song.id;

    if (!playlistId || !songId) {
      setError("Geçersiz playlist veya şarkı kimliği.");
      return;
    }

    setLoading(true);
    setError("");

    try {
      await onAdd(playlistId, songId);
      setOpen(false);
    } catch (err) {
      if (err.message.includes("Oturum süresi dolmuş")) {
        onPromptLogin?.();
      } else if (err.message.includes("yetkin yok")) {
        setError("Bu playlist'e şarkı eklemek için yetkiniz yok.");
      } else {
        setError(err.message || "Şarkı playlist'e eklenemedi.");
      }
    } finally {
      setLoading(false);
    }
  }

  return (
      <div className={`add-to-playlist-dropdown ${className ?? ""}`} ref={ref}>
        <button
            className="dropdown-trigger"
            onClick={handleToggle}
            disabled={loading}
            aria-label="Playlist'e ekle"
            title="Playlist'e ekle"
        >
          ⋮
        </button>

        {open && (
            <div className="dropdown-menu">
              <div className="dropdown-header">Playlist'e ekle</div>

              {error && (
                  <div className="dropdown-error" role="alert">
                    {error}
                  </div>
              )}

              {playlistsLoading ? (
                  <div className="dropdown-item disabled">
                    Playlist'ler yükleniyor...
                  </div>
              ) : playlists.length === 0 ? (
                  <div className="dropdown-item disabled">
                    Playlist'in yok.
                  </div>
              ) : (
                  playlists.map((playlist) => {
                    const playlistId = playlist.publicId ?? playlist.id;
                    const playlistName = playlist.name ?? playlist.title ?? "İsimsiz";
                    return (
                        <button
                            key={playlistId}
                            className="dropdown-item"
                            onClick={() => handleSelect(playlist)}
                            disabled={loading}
                        >
                          <span className="dropdown-item-icon">♫</span>
                          <span className="dropdown-item-text">{playlistName}</span>
                        </button>
                    );
                  })
              )}
            </div>
        )}
      </div>
  );
}
