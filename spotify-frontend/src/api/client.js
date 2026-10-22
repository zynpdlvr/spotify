
const API_BASE = "http://localhost:8081/api/v1";

export async function apiRequest(path, options = {}) {
    const token = localStorage.getItem("accessToken");

    const response = await fetch(`${API_BASE}${path}`, {
        ...options,
        headers: {
            ...(options.body ? { "Content-Type": "application/json" } : {}),
            ...options.headers,
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
    });

    if (!response.ok) {
        const message =
            response.status === 401
                ? "Oturum süresi dolmuş. Yeniden giriş yapmalısın."
                : response.status === 403
                    ? "Bu işlem için yetkin yok."
                    : `API hatası: ${response.status}`;

        throw new Error(message);
    }

    if (response.status === 204) return null;

    return response.json();
}

export { API_BASE };
