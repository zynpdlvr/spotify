export default function Topbar({
  search,
  onSearchChange,
  loggedIn,
  onProfileClick,
}) {
  return (
    <header className="topbar">
      <div className="search-box">
        <span>⌕</span>
        <input
          aria-label="Şarkı, sanatçı veya albüm ara"
          placeholder="Şarkı, sanatçı veya albüm ara..."
          value={search}
          onChange={(event) => onSearchChange(event.target.value)}
        />
        {search && (
          <button
            className="clear-search"
            onClick={() => onSearchChange("")}
            aria-label="Aramayı temizle"
          >
            ×
          </button>
        )}
      </div>

      <div className="profile-menu">
        <button className="profile-button" onClick={onProfileClick}>
          <span className="profile-avatar">{loggedIn ? "✓" : "Z"}</span>
          <span>{loggedIn ? "Çıkış yap" : "Giriş yap"}</span>
        </button>
      </div>
    </header>
  );
}
