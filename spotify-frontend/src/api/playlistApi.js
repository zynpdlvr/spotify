import { apiRequest } from "./client";

/**
 * Şarkıyı playlist'e ekler.
 * PUT /playlists/{publicId}/songs/{songPublicId}
 */
export async function addSongToPlaylist(playlistPublicId, songPublicId) {
    const data = await apiRequest(
        `/playlists/${encodeURIComponent(playlistPublicId)}/songs/${encodeURIComponent(songPublicId)}`,
        { method: "PUT" }
    );
    return data;
}

/**
 * Şarkıyı playlist'ten çıkarır.
 * DELETE /playlists/{publicId}/songs/{songPublicId}
 */
export async function removeSongFromPlaylist(playlistPublicId, songPublicId) {
    const data = apiRequest(
        `/playlists/${encodeURIComponent(playlistPublicId)}/songs/${encodeURIComponent(songPublicId)}`,
        { method: "DELETE" }
    );
    return data;
}
