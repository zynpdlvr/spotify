import { useEffect, useState } from "react";
import { apiRequest } from "../api/client";
import { addFavorite, removeFavorite } from "../api/favoriteApi";

export function useFavorites(authVersion) {
    const [favorites, setFavorites] = useState([]);
    const [favoritesLoading, setFavoritesLoading] = useState(false);
    const [favoritesError, setFavoritesError] = useState("");
    const [favoriteActionLoading, setFavoriteActionLoading] = useState(false);

    useEffect(() => {
        const controller = new AbortController();

        async function fetchFavorites() {
            const token = localStorage.getItem("accessToken");

            if (!token) {
                setFavorites([]);
                setFavoritesError("");
                setFavoritesLoading(false);
                return;
            }

            try {
                setFavoritesLoading(true);
                setFavoritesError("");

                const data = await apiRequest("/me/favorites", {
                    signal: controller.signal,
                });

                setFavorites(
                    Array.isArray(data)
                        ? data.map((song) => song.publicId).filter(Boolean)
                        : []
                );
            } catch (err) {
                if (err.name !== "AbortError") {
                    setFavoritesError(
                        err.message || "Favoriler yüklenemedi."
                    );
                }
            } finally {
                if (!controller.signal.aborted) {
                    setFavoritesLoading(false);
                }
            }
        }

        fetchFavorites();

        return () => controller.abort();
    }, [authVersion]);


    async function toggleFavorite(songId) {
        if (!songId || favoriteActionLoading) return;

        const wasFavorite = favorites.includes(songId);

        try {
            setFavoriteActionLoading(true);
            setFavoritesError("");

            if (wasFavorite) {
                await removeFavorite(songId);
            } else {
                await addFavorite(songId);
            }

            // Backend isteği başarılı olduktan sonra state'i güncelle.
            setFavorites((previous) =>
                wasFavorite
                    ? previous.filter((id) => id !== songId)
                    : previous.includes(songId)
                        ? previous
                        : [...previous, songId]
            );
        } catch (err) {
            setFavoritesError(
                err.message || "Favori işlemi başarısız oldu."
            );
        } finally {
            setFavoriteActionLoading(false);
        }
    }

    return {
        favorites,
        setFavorites,
        favoritesLoading,
        favoritesError,
        setFavoritesError,
        favoriteActionLoading,
        toggleFavorite,
    };
}