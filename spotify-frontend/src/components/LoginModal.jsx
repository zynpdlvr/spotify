export default function LoginModal({
  email,
  password,
  loading,
  error,
  onEmailChange,
  onPasswordChange,
  onSubmit,
  onClose,
}) {
  return (
    <div
      className="login-overlay"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) onClose();
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
          onClick={onClose}
          aria-label="Giriş penceresini kapat"
        >
          ×
        </button>

        <div className="login-brand">♫ spotify</div>
        <h2 id="login-title">Tekrar hoş geldin</h2>
        <p className="login-subtitle">
          Hesabına giriş yap ve müziğine devam et.
        </p>

        <form onSubmit={onSubmit}>
          <label htmlFor="login-email">E-posta</label>
          <input
            id="login-email"
            type="email"
            autoComplete="email"
            placeholder="ornek@email.com"
            value={email}
            onChange={(event) => onEmailChange(event.target.value)}
            required
          />

          <label htmlFor="login-password">Şifre</label>
          <input
            id="login-password"
            type="password"
            autoComplete="current-password"
            placeholder="Şifreni gir"
            value={password}
            onChange={(event) => onPasswordChange(event.target.value)}
            required
          />

          {error && (
            <p className="login-error" role="alert">
              {error}
            </p>
          )}

          <button className="login-submit" type="submit" disabled={loading}>
            {loading ? "Giriş yapılıyor..." : "Giriş yap"}
          </button>
        </form>

        <p className="login-footer">
          Henüz hesabın yok mu? Önce mevcut kayıt olma endpoint'ini kullanabilirsin.
        </p>
      </section>
    </div>
  );
}
