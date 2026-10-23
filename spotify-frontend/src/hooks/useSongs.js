
import { useEffect, useState } from "react";
import { getSongs } from "../api/songApi";
import { COVER_COLORS } from "../utils/constants";

export function useSongs(query, refreshKey = 0) {
    const [songs, setSongs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const controller = new AbortController();

        async function fetchSongs() {
            try {
                setLoading(true);
                setError("");

                const songList = await getSongs({
                    query,
                    signal: controller.signal,
                });

                const mappedSongs = songList.map((song, index) => ({
                    ...song,
                    id: song.publicId,
                    color: COVER_COLORS[index % COVER_COLORS.length],
                    icon: "♫",
                    time: "—",
                }));

                setSongs(mappedSongs);
            } catch (err) {
                if (err.name !== "AbortError") {
                    setError(err.message || "Şarkılar yüklenemedi.");
                }
            } finally {
                if (!controller.signal.aborted) {
                    setLoading(false);
                }
            }
        }

        fetchSongs();

        return () => controller.abort();
    }, [query, refreshKey]);

    return {
        songs,
        loading,
        error,
    };
}
