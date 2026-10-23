import { useState } from "react";
import { roleLabel } from "../utils/constants";

export default function ProfileModal({
  profileOpen,
  setProfileOpen,
  user,
  role,
  loading,
  error,
  handleUpdate,
  handleDelete,
}) {
  const [displayName, setDisplayName] = useState(user?.displayName ?? "");
  const [email, setEmail] = useState(user?.email ?? "");
  const [localError, setLocalError] = useState("");
  const [localLoading, setLocalLoading] = useState(false);

  if (!profileOpen) return null;

  function handleClose() {
    setProfileOpen(false);
    setLocalError("");
  }

  return (
      <div
          className="login-overlay"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) {
              handleClose();
            }
          }}
      >
        <section
            className="login-modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="profile-title"
        >
          <button
              className="login-close"
              type="button"
              onClick={handleClose}
              aria-label="Profil penceresini kapat"
          >
            ×
          </button>

          <div className="login-brand">♫ profil</div>

          <h2 id="profile-title">Profil</h2>
          <p className="login-subtitle">
            Hesap bilgilerini görüntüle ve düzenle.
          </p>

          <form onSubmit={async (event) => {
              event.preventDefault();
              setLocalError("");
              setLocalLoading(true);
              try {
                  await handleUpdate({
                      displayName: displayName.trim() || undefined,
                      email: email.trim().toLowerCase() || undefined,
                  });
                  handleClose();
              } catch (err) {
                  setLocalError(err.message || "Profil güncellenemedi.");
              } finally {
                  setLocalLoading(false);
              }
          }}>
            <label htmlFor="profile-displayname">İsim</label>
            <input
                id="profile-displayname"
                type="text"
                value={displayName}
                onChange={(event) => setDisplayName(event.target.value)}
                maxLength={80}
            />

            <label htmlFor="profile-email">E-posta</label>
            <input
                id="profile-email"
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                maxLength={190}
            />

            {role && (
                <div className="profile-role">
                  <label>Rol</label>
                  <div className="profile-role-value">
                    <span className={`role-badge role-${role.toLowerCase()}`}>
                      {roleLabel(role)}
                    </span>
                  </div>
                </div>
            )}

            {(localError || error) && (
                <p className="login-error" role="alert">
                  {localError || error}
                </p>
            )}

            <button
                className="login-submit"
                type="submit"
                disabled={localLoading || loading}
            >
              {localLoading || loading ? "Kaydediliyor..." : "Güncelle"}
            </button>
          </form>

          <div className="profile-actions">
            <button
                type="button"
                className="profile-delete"
                onClick={async () => {
                  const confirmed = window.confirm(
                      "Hesabını silmek istediğine emin misin? Bu işlem geri alınamaz."
                  );
                  if (!confirmed) return;

                  try {
                      await handleDelete();
                      handleClose();
                  } catch (err) {
                      setLocalError(err.message || "Hesap silinemedi.");
                  }
                }}
            >
              Hesabı sil
            </button>
          </div>
        </section>
      </div>
  );
}
