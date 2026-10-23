
import { useState } from "react";
import { createSong } from "../api/songApi.js";

export default function SongCreatePage({ albums = [], onCreated, onCancel }) {
    const [form, setForm] = useState({
        title: "",
        artist: "",
        albumId: "",
        audioUrl: "",
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    function update(event) {
        setForm((current) => ({
            ...current,
            [event.target.name]: event.target.value,
        }));
    }

    async function submit(event) {
        event.preventDefault();
        setLoading(true);
        setError("");
        setSuccess("");

        try {
            await createSong({
                title: form.title.trim(),
                artist: form.artist.trim(),
                albumId: form.albumId ? Number(form.albumId) : null,
                audioUrl: form.audioUrl.trim(),
            });

            setForm({
                title: "",
                artist: "",
                albumId: "",
                audioUrl: "",
            });

            setSuccess("Şarkı başarıyla oluşturuldu.");
            onCreated?.();
        } catch (err) {
            setError(err.message || "Şarkı oluşturulamadı.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <section className="create-page">
            <p className="eyebrow">KATALOĞU GENİŞLET</p>
            <h1>Şarkı oluştur</h1>
            <p className="create-page-intro">
                Şarkı bilgilerini girerek kataloğa yeni bir şarkı ekle.
            </p>

            <form className="create-form" onSubmit={submit}>
                <label htmlFor="song-title">Şarkı adı *</label>
                <input
                    id="song-title"
                    name="title"
                    value={form.title}
                    onChange={update}
                    maxLength={160}
                    required
                    placeholder="Şarkı adı"
                />

                <label htmlFor="song-artist">Sanatçı adı *</label>
                <input
                    id="song-artist"
                    name="artist"
                    value={form.artist}
                    onChange={update}
                    maxLength={160}
                    required
                    placeholder="Sanatçı adı"
                />

                <label htmlFor="song-album">Albüm</label>
                <select
                    id="song-album"
                    name="albumId"
                    value={form.albumId}
                    onChange={update}
                >
                    <option value="">Albüm seçilmedi</option>
                    {albums.map((album) => (
                        <option key={album.id} value={album.id}>
                            {album.name}
                        </option>
                    ))}
                </select>

                <label htmlFor="song-audio-url">Ses dosyası URL'si *</label>
                <input
                    id="song-audio-url"
                    name="audioUrl"
                    type="url"
                    value={form.audioUrl}
                    onChange={update}
                    maxLength={1000}
                    required
                    placeholder="https://example.com/song.mp3"
                />

                {error && (
                    <p className="form-message form-error" role="alert">
                        {error}
                    </p>
                )}

                {success && (
                    <p className="form-message form-success" role="status">
                        {success}
                    </p>
                )}

                <div className="create-form-actions">
                    <button
                        type="button"
                        className="secondary-button"
                        onClick={onCancel}
                    >
                        İptal
                    </button>

                    <button type="submit" disabled={loading}>
                        {loading ? "Kaydediliyor..." : "Şarkıyı oluştur"}
                    </button>
                </div>
            </form>
        </section>
    );
}