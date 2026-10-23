import { useState } from "react";
import { getMe, updateMe, deleteMe } from "../api/userApi";

export function useUser({ loggedIn, onUserUpdated, onLogout }) {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    async function fetchProfile() {
        if (!loggedIn) return null;

        try {
            setLoading(true);
            setError("");
            const user = await getMe();
            onUserUpdated?.(user);
            return user;
        } catch (err) {
            setError(err.message || "Profil yüklenemedi.");
            return null;
        } finally {
            setLoading(false);
        }
    }

    async function updateProfile(data) {
        try {
            setLoading(true);
            setError("");
            const updated = await updateMe(data);
            onUserUpdated?.(updated);
            return updated;
        } catch (err) {
            setError(err.message || "Profil güncellenemedi.");
            throw err;
        } finally {
            setLoading(false);
        }
    }

    async function deleteProfile() {
        try {
            setLoading(true);
            setError("");
            await deleteMe();
            onLogout?.();
        } catch (err) {
            setError(err.message || "Hesap silinemedi.");
            throw err;
        } finally {
            setLoading(false);
        }
    }

    return {
        loading,
        error,
        setError,
        fetchProfile,
        updateProfile,
        deleteProfile,
    };
}
