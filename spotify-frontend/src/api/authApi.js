
import { API_BASE } from "./client";

export async function loginRequest(email, password) {
    const response = await fetch(`${API_BASE}/auth/login`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            email: email.trim(),
            password,
        }),
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
        throw new Error(
            data.message ??
            data.detail ??
            (response.status === 401
                ? "E-posta veya şifre hatalı."
                : `Giriş başarısız: ${response.status}`)
        );
    }

    const token = data.accessToken ?? data.token;

    if (!token) {
        throw new Error("Giriş yanıtında token bulunamadı.");
    }

    return token;
}

export function logoutRequest() {
    localStorage.removeItem("accessToken");
}
