
import { useEffect, useState } from "react";
import { getAlbumsList } from "../api/AlbumApi";
import { COVER_COLORS } from "../utils/constants";

export function useAlbums(authVersion) {
    const [albums, setAlbums] = useState([]);
    const [albumsLoading, setAlbumsLoading] = useState(true);
    const [albumsError, setAlbumsError] = useState("");

    useEffect(() => {
        const controller = new AbortController();

        async function fetchAlbums() {
            try {
                setAlbumsLoading(true);
                setAlbumsError("");

                const albumList = await getAlbumsList();

                setAlbums(
                    albumList.map((album, index) => ({
                        ...album,
                        id: album.publicId ?? album.id,
                        color: COVER_COLORS[index % COVER_COLORS.length],
                        icon: "♫",
                    }))
                );
            } catch (err) {
                if (err.name !== "AbortError") {
                    setAlbums([]);
                    setAlbumsError(err.message || "Albümler yüklenemedi.");
                }
            } finally {
                if (!controller.signal.aborted) {
                    setAlbumsLoading(false);
                }
            }
        }

        fetchAlbums();

        return () => controller.abort();
    }, [authVersion]);

    return {
        albums,
        setAlbums,
        albumsLoading,
        albumsError,
        setAlbumsError,
    };
}
