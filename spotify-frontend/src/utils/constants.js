export const API_BASE = "http://localhost:8081/api/v1";
export const COVER_COLORS = ["purple", "pink", "blue", "orange", "red"];

// Backend role values (match com.example.spotify.entity.Role)
export const ROLE = {
    USER: "USER",
    ADMIN: "ADMIN",
    ARTIST: "ARTIST",
};

/**
 * Returns a human-readable label for a role value.
 */
export function roleLabel(role) {
    switch (role) {
        case ROLE.ADMIN:
            return "Admin";
        case ROLE.ARTIST:
            return "Artist";
        case ROLE.USER:
        default:
            return "User";
    }
}

/**
 * Checks if the given role has the ARTIST authority
 * (can create, update, delete songs and albums).
 */
export function isArtist(role) {
    return role === ROLE.ARTIST || role === ROLE.ADMIN;
}

/**
 * Checks if the given role has the ADMIN authority.
 */
export function isAdmin(role) {
    return role === ROLE.ADMIN;
}
