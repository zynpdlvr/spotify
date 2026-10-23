
import { useState } from "react";
import { createAlbum } from "../api/albumApi.js";

export default function AlbumCreatePage({ onCreated, onCancel }) {
    const [form, setForm] = useState({
        name: "",
        artistName: "",
        description: "",
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
            await createAlbum({
                name: form.name.trim(),
                artistName: form.artistName.trim(),
                description: form.description.trim(),
            });

            setForm({ name: "", artistName: "", description: "" });
            setSuccess("Albüm başarıyla oluşturuldu.");
            onCreated?.();
        } catch (err) {
            setError(err.message || "Albüm oluşturulamadı.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <section className="create-page">
            <p className="eyebrow">KATALOĞU GENİŞLET</p>
            <h1>Albüm oluştur</h1>
            <p className="create-page-intro">
                Albüm bilgilerini girerek kataloğa yeni bir albüm ekle.
            </p>

            <form className="create-form" onSubmit={submit}>
                <label htmlFor="album-name">Albüm adı *</label>
                <input
                    id="album-name"
                    name="name"
                    value={form.name}
                    onChange={update}
                    maxLength={120}
                    required
                    placeholder="Albüm adı"
                />

                <label htmlFor="album-artist">Sanatçı adı *</label>
                <input
                    id="album-artist"
                    name="artistName"
                    value={form.artistName}
                    onChange={update}
                    maxLength={120}
                    required
                    placeholder="Sanatçı adı"
                />

                <label htmlFor="album-description">Açıklama</label>
                <textarea
                    id="album-description"
                    name="description"
                    value={form.description}
                    onChange={update}
                    maxLength={500}
                    rows={4}
                    placeholder="Albüm hakkında kısa bir açıklama"
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
                    <button type="button" className="secondary-button" onClick={onCancel}>
                        İptal
                    </button>
                    <button type="submit" disabled={loading}>
                        {loading ? "Kaydediliyor..." : "Albümü oluştur"}
                    </button>
                </div>
            </form>
        </section>
    );
}
