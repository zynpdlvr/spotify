
import { API_BASE } from "../utils/constants";

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

    return data;
}

export async function registerRequest(displayName, email, password) {
    const response = await fetch(`${API_BASE}/auth/register`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            displayName: displayName.trim(),
            email: email.trim().toLowerCase(),
            password,
        }),
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
        throw new Error(
            data.message ??
            data.detail ??
            (response.status === 409
                ? "Bu e-posta adresi zaten kayıtlı."
                : `Kayıt başarısız: ${response.status}`)
        );
    }

    const token = data.accessToken ?? data.token;

    if (!token) {
        throw new Error("Kayıt yanıtında token bulunamadı.");
    }

    return data;
}

export function logoutRequest() {
    localStorage.removeItem("accessToken");
}
