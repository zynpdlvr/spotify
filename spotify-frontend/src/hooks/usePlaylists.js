import { useEffect, useState } from "react";
import { apiRequest } from "../api/client";
import { COVER_COLORS } from "../utils/constants";
import { getEmbeddedList } from "../utils/hateoas";

export function usePlaylists(authVersion) {
    const [playlists, setPlaylists] = useState([]);
    const [playlistsLoading, setPlaylistsLoading] = useState(true);
    const [playlistsError, setPlaylistsError] = useState("");

    useEffect(() => {
        const controller = new AbortController();

        async function fetchPlaylists() {
            const token = localStorage.getItem("accessToken");

            if (!token) {
                setPlaylists([]);
                setPlaylistsError("");
                setPlaylistsLoading(false);
                return;
            }

            try {
                setPlaylistsLoading(true);
                setPlaylistsError("");

                const data = await apiRequest("/playlists", {
                    signal: controller.signal,
                });

                const playlistList = getEmbeddedList(data);

                setPlaylists(
                    playlistList.map((playlist, index) => ({
                        ...playlist,
                        id: playlist.publicId ?? playlist.id,
                        title: playlist.name,
                        color: COVER_COLORS[index % COVER_COLORS.length],
                        icon: "♫",
                    }))
                );
            } catch (err) {
                if (err.name !== "AbortError") {
                    setPlaylistsError(
                        err.message || "Playlist'ler yüklenemedi."
                    );
                }
            } finally {
                if (!controller.signal.aborted) {
                    setPlaylistsLoading(false);
                }
            }
        }

        fetchPlaylists();

        return () => controller.abort();
    }, [authVersion]);

    return {
        playlists,
        setPlaylists,
        playlistsLoading,
        playlistsError,
        setPlaylistsError,
    };
}