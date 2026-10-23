
import { apiRequest } from "./client";
import { getEmbeddedList } from "../utils/hateoas";

// Bir albümün detaylarını getirir.
export function getAlbumById(publicId) {
    return apiRequest(`/albums/${encodeURIComponent(publicId)}`);
}

// Tüm albümleri listeler (HATEOAS CollectionModel).
export async function getAlbumsList() {
    const data = await apiRequest("/albums");
    return getEmbeddedList(data);
}


export function createAlbum(payload) {
    return apiRequest("/albums", {
        method: "POST",
        body: JSON.stringify(payload),
    });
}

