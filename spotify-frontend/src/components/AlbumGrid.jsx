
import Cover from "./Cover";

export default function AlbumGrid({
                                      albums,
                                      albumsLoading,
                                      albumsError,
                                      onSelectAlbum,
                                  }) {
    return (
        <section className="album-section">
            <div className="section-heading">
                <h2>Albümler</h2>
                <span className="muted">
          {albums.length} albüm
        </span>
            </div>

            {albumsLoading && (
                <p className="empty-state">
                    Albümler yükleniyor...
                </p>
            )}

            {albumsError && (
                <p className="empty-state" role="alert">
                    {albumsError}
                </p>
            )}

            {!albumsLoading &&
                !albumsError &&
                albums.length === 0 && (
                    <p className="empty-state">
                        Henüz albüm bulunamadı.
                    </p>
                )}

            <div className="recent-grid">
                {!albumsLoading &&
                    !albumsError &&
                    albums.map((album) => (
                        <button
                            type="button"
                            className="recent-card"
                            key={album.publicId ?? album.id}
                            onClick={() => onSelectAlbum(album)}
                        >
                            <Cover
                                color={album.color}
                                icon={album.icon}
                            />

                            <strong>
                                {album.name ?? "İsimsiz albüm"}
                            </strong>

                            <span>
                {album.artistName ??
                    "Bilinmeyen sanatçı"}
              </span>
                        </button>
                    ))}
            </div>
        </section>
    );
}
