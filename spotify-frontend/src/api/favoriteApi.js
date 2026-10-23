
import { apiRequest } from "./client";

export function addFavorite(songId) {
    return apiRequest(
        `/me/favorites/${encodeURIComponent(songId)}`,
        { method: "PUT" }
    );
}

export function removeFavorite(songId) {
    return apiRequest(
        `/me/favorites/${encodeURIComponent(songId)}`,
        { method: "DELETE" }
    );
}
