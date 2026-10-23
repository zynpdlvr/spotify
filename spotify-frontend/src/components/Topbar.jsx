import { roleLabel } from "../utils/constants";

export default function Topbar({
  search,
  setSearch,
  loggedIn,
  user,
  role,
  onAuthAction,
  onProfileClick,
}) {
  return (
      <header className="topbar">
        <div className="search-box">
          <span>⌕</span>
          <input
              id="search-input"
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
          {loggedIn && user && (
              <button
                  className="profile-button"
                  onClick={onProfileClick}
                  title="Profil"
              >
    <span className="profile-avatar">
      {user.displayName?.charAt(0)?.toUpperCase() ?? "U"}
    </span>
                <span>{user.displayName ?? "Kullanıcı"}</span>
                {role && role !== "USER" && (
                    <span className={`role-badge role-${role.toLowerCase()}`}>
                      {roleLabel(role)}
                    </span>
                )}
              </button>
          )}

          <button
              className="profile-button"
              onClick={onAuthAction}
          >
    <span className="profile-avatar">
      {loggedIn ? "✓" : "Z"}
    </span>
            <span>{loggedIn ? "Çıkış yap" : "Giriş yap"}</span>
          </button>
        </div>
      </header>
  );
}
