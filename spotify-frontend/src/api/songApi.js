
import { apiRequest } from "./client";
import { getEmbeddedList } from "../utils/hateoas";

export async function getSongs({ query, signal } = {}) {
    const params = new URLSearchParams();
    if (query) params.set("q", query);

    const path = params.toString() ? `/songs?${params.toString()}` : "/songs";
    const data = await apiRequest(path, { signal });
    return getEmbeddedList(data);
}


export function createSong(payload) {
    return apiRequest("/songs", {
        method: "POST",
        body: JSON.stringify(payload),
    });
}

