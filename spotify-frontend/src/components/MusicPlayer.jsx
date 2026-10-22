import Cover from "./Cover";

export default function MusicPlayer({
  activeSong,
  playing,
  favorites,
  favoriteActionLoading,
  songsCount,
  onToggleFavorite,
  onPrevious,
  onPlayPause,
  onNext,
}) {
  return (
    <footer className="player">
      <div className="now-playing">
        {activeSong ? (
          <>
            <Cover color={activeSong.color} icon={activeSong.icon} small />
            <div>
              <strong>{activeSong.title}</strong>
              <small>{activeSong.artist}</small>
            </div>
            <button
              className={`heart-button ${favorites.includes(activeSong.id) ? "is-favorite" : ""}`}
              disabled={favoriteActionLoading}
              onClick={() => onToggleFavorite(activeSong.id)}
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
          <button aria-label="Önceki şarkı" disabled={songsCount === 0} onClick={onPrevious}>
            |◀
          </button>
          <button
            className="play-button"
            aria-label={playing ? "Duraklat" : "Oynat"}
            disabled={!activeSong}
            onClick={onPlayPause}
          >
            {playing ? "Ⅱ" : "▶"}
          </button>
          <button aria-label="Sonraki şarkı" disabled={songsCount === 0} onClick={onNext}>
            ▶|
          </button>
        </div>

        <div className="progress-row">
          <span>0:00</span>
          <div className="progress-track"><div /></div>
          <span>{activeSong?.time ?? "—"}</span>
        </div>
      </div>

      <div className="player-extra">
        <span>♫</span>
        <div className="volume-track"><div /></div>
      </div>
    </footer>
  );
}
