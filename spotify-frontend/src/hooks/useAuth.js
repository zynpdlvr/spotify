import { useState } from "react";
import { ROLE, isArtist, isAdmin } from "../utils/constants";

export function useAuth() {
    const [loggedIn, setLoggedIn] = useState(
        () => Boolean(localStorage.getItem("accessToken"))
    );

    const [user, setUser] = useState(() => {
        const stored = localStorage.getItem("user");
        return stored ? JSON.parse(stored) : null;
    });

    const [authVersion, setAuthVersion] = useState(0);
    const [loginOpen, setLoginOpen] = useState(false);

    // Derived role from user object
    const role = user?.role ?? null;

    function handleLoginSuccess(response) {
        const token = response.accessToken;
        const userInfo = response.user;

        localStorage.setItem("accessToken", token);
        if (userInfo) {
            localStorage.setItem("user", JSON.stringify(userInfo));
            setUser(userInfo);
        }
        setLoggedIn(true);
        setAuthVersion((version) => version + 1);
        setLoginOpen(false);
    }

    function handleLogout() {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("user");

        setLoggedIn(false);
        setUser(null);
        setAuthVersion((version) => version + 1);
        setLoginOpen(false);
    }

    return {
        loggedIn,
        setLoggedIn,
        user,
        setUser,
        role,
        authVersion,
        loginOpen,
        setLoginOpen,
        handleLoginSuccess,
        handleLogout,
        ROLE,
        isArtist: isArtist(role),
        isAdmin: isAdmin(role),
    };
}