import { apiRequest } from "./client";

export async function getMe() {
    return apiRequest("/users/me");
}

export async function updateMe(data) {
    return apiRequest("/users/me", {
        method: "PATCH",
        body: JSON.stringify(data),
    });
}

export async function deleteMe() {
    return apiRequest("/users/me", { method: "DELETE" });
}
