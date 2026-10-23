import { useMemo } from "react";

export function useVisibleSongs({
                                    filteredSongs,
                                    activeMenu,
                                    favorites,
                                    selectedAlbum,
                                    selectedAlbumSongIds,
                                    selectedPlaylistData,
                                    selectedPlaylistSongIds,
                                }) {
    return useMemo(
        () =>
            filteredSongs.filter((song) => {
                if (
                    activeMenu === "Beğenilen Şarkılar" &&
                    !favorites.includes(song.id)
                ) {
                    return false;
                }

                if (
                    selectedAlbum &&
                    !selectedAlbumSongIds.includes(song.id)
                ) {
                    return false;
                }

                if (
                    selectedPlaylistData &&
                    !selectedPlaylistSongIds.includes(song.id)
                ) {
                    return false;
                }

                return true;
            }),
        [
            filteredSongs,
            activeMenu,
            favorites,
            selectedAlbum,
            selectedAlbumSongIds,
            selectedPlaylistData,
            selectedPlaylistSongIds,
        ]
    );
}