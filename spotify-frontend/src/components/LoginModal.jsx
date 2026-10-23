import { useState } from "react";

export default function LoginModal({
  loginOpen,
  setLoginOpen,
  loginEmail,
  setLoginEmail,
  loginPassword,
  setLoginPassword,
  loginLoading,
  loginError,
  handleLogin,
  handleRegister,
}) {
  const [mode, setMode] = useState("login");
  const [registerDisplayName, setRegisterDisplayName] = useState("");
  const [registerError, setRegisterError] = useState("");
  const [registerLoading, setRegisterLoading] = useState(false);
  const [localLoginError, setLocalLoginError] = useState("");

  if (!loginOpen) return null;

  const isLogin = mode === "login";
  const displayedLoginError = localLoginError || loginError;

  function resetForms() {
    setLoginEmail("");
    setLoginPassword("");
    setRegisterDisplayName("");
    setRegisterError("");
    setLocalLoginError("");
  }

  function handleClose() {
    setMode("login");
    setLoginOpen(false);
    resetForms();
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
            aria-labelledby="login-title"
        >
          <button
              className="login-close"
              type="button"
              onClick={handleClose}
              aria-label="Pencereyi kapat"
          >
            ×
          </button>

          <div className="login-brand">♫ spotify</div>

          <h2 id="login-title">{isLogin ? "Tekrar hoş geldin" : "Hesap oluştur"}</h2>
          <p className="login-subtitle">
            {isLogin
                ? "Hesabına giriş yap ve müziğine devam et."
                : "Yeni bir hesap oluştur ve müziğini keşfet."}
          </p>

          {isLogin ? (
              <form onSubmit={async (event) => {
                  event.preventDefault();
                  setLocalLoginError("");
                  await handleLogin(event);
              }}>
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

                {displayedLoginError && (
                    <p className="login-error" role="alert">
                      {displayedLoginError}
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
          ) : (
              <form onSubmit={async (event) => {
                  event.preventDefault();
                  setRegisterError("");
                  setRegisterLoading(true);
                  try {
                      await handleRegister(registerDisplayName, loginEmail, loginPassword);
                      handleClose();
                  } catch (err) {
                      setRegisterError(err.message || "Kayıt başarısız oldu.");
                  } finally {
                      setRegisterLoading(false);
                  }
              }}>
                <label htmlFor="register-displayname">İsim</label>
                <input
                    id="register-displayname"
                    type="text"
                    autoComplete="name"
                    placeholder="Adını gir"
                    value={registerDisplayName}
                    onChange={(event) => setRegisterDisplayName(event.target.value)}
                    required
                    maxLength={80}
                />

                <label htmlFor="register-email">E-posta</label>
                <input
                    id="register-email"
                    type="email"
                    autoComplete="email"
                    placeholder="ornek@email.com"
                    value={loginEmail}
                    onChange={(event) => setLoginEmail(event.target.value)}
                    required
                    maxLength={190}
                />

                <label htmlFor="register-password">Şifre</label>
                <input
                    id="register-password"
                    type="password"
                    autoComplete="new-password"
                    placeholder="En az 8 karakter"
                    value={loginPassword}
                    onChange={(event) => setLoginPassword(event.target.value)}
                    required
                    minLength={8}
                    maxLength={72}
                />

                {registerError && (
                    <p className="login-error" role="alert">
                      {registerError}
                    </p>
                )}

                <button
                    className="login-submit"
                    type="submit"
                    disabled={registerLoading}
                >
                  {registerLoading ? "Kaydediliyor..." : "Kayıt ol"}
                </button>
              </form>
          )}

          <p className="login-footer">
            {isLogin
                ? "Henüz hesabın yok mu? "
                : "Zaten hesabın var mı? "}
            <button
                type="button"
                className="login-switch"
                onClick={() => {
                    setMode(isLogin ? "register" : "login");
                    setRegisterError("");
                    setLocalLoginError("");
                }}
            >
              {isLogin ? "Kayıt ol" : "Giriş yap"}
            </button>
          </p>
        </section>
      </div>
  );
}
