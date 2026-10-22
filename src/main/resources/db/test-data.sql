-- Spotify backend: larger deterministic test dataset
-- Run from the project root:
-- Get-Content -Raw .\test-data.sql | docker compose exec -T db psql -U spotify_user -d spotify
--
-- This file is safe to rerun: stable public_id UUIDs and ON CONFLICT keep rows
-- deterministic. Artist is currently text on Album/Song, not a separate table.

BEGIN;

-- Albums (8)
INSERT INTO album (public_id, created_at, updated_at, name, description, artist_name)
VALUES
    ('11111111-1111-4111-8111-111111111111', NOW(), NOW(), 'Test Album', 'Backend API test album', 'Test Artist'),
    ('11111111-1111-4111-8111-111111111112', NOW(), NOW(), 'Midnight Signals', 'Electronic and synth-pop collection', 'Nova Echo'),
    ('11111111-1111-4111-8111-111111111113', NOW(), NOW(), 'Paper Skies', 'Indie pop songs for long drives', 'Mira Lane'),
    ('11111111-1111-4111-8111-111111111114', NOW(), NOW(), 'Golden Hour', 'Warm acoustic recordings', 'The Northlines'),
    ('11111111-1111-4111-8111-111111111115', NOW(), NOW(), 'City Lights', 'Modern alternative album', 'Nova Echo'),
    ('11111111-1111-4111-8111-111111111116', NOW(), NOW(), 'Quiet Oceans', 'Ambient and instrumental pieces', 'Atlas Bloom'),
    ('11111111-1111-4111-8111-111111111117', NOW(), NOW(), 'Neon Garden', 'Upbeat electronic tracks', 'Luna Park'),
    ('11111111-1111-4111-8111-111111111118', NOW(), NOW(), 'Small Wonders', 'Acoustic indie stories', 'Mira Lane')
    ON CONFLICT (public_id) DO UPDATE SET
    name = EXCLUDED.name,
                                   description = EXCLUDED.description,
                                   artist_name = EXCLUDED.artist_name,
                                   updated_at = NOW();

-- Songs (24), spread across albums/artists to test search, sort, pages and filters.
WITH song_data(public_id, title, artist, audio_url, album_uuid) AS (
    VALUES
        ('22222222-2222-4222-8222-222222222201'::uuid, 'Signal Fires', 'Nova Echo', 'https://example.com/signal-fires.mp3', '11111111-1111-4111-8111-111111111112'::uuid),
        ('22222222-2222-4222-8222-222222222202'::uuid, 'After Midnight', 'Nova Echo', 'https://example.com/after-midnight.mp3', '11111111-1111-4111-8111-111111111112'::uuid),
        ('22222222-2222-4222-8222-222222222203'::uuid, 'Glass Satellites', 'Nova Echo', 'https://example.com/glass-satellites.mp3', '11111111-1111-4111-8111-111111111112'::uuid),
        ('22222222-2222-4222-8222-222222222204'::uuid, 'Paper Planes', 'Mira Lane', 'https://example.com/paper-planes.mp3', '11111111-1111-4111-8111-111111111113'::uuid),
        ('22222222-2222-4222-8222-222222222205'::uuid, 'Blue Postcards', 'Mira Lane', 'https://example.com/blue-postcards.mp3', '11111111-1111-4111-8111-111111111113'::uuid),
        ('22222222-2222-4222-8222-222222222206'::uuid, 'Slow Motion', 'Mira Lane', 'https://example.com/slow-motion.mp3', '11111111-1111-4111-8111-111111111113'::uuid),
        ('22222222-2222-4222-8222-222222222207'::uuid, 'Golden Roads', 'The Northlines', 'https://example.com/golden-roads.mp3', '11111111-1111-4111-8111-111111111114'::uuid),
        ('22222222-2222-4222-8222-222222222208'::uuid, 'Wooden Heart', 'The Northlines', 'https://example.com/wooden-heart.mp3', '11111111-1111-4111-8111-111111111114'::uuid),
        ('22222222-2222-4222-8222-222222222209'::uuid, 'Open Windows', 'The Northlines', 'https://example.com/open-windows.mp3', '11111111-1111-4111-8111-111111111114'::uuid),
        ('22222222-2222-4222-8222-222222222210'::uuid, 'City Static', 'Nova Echo', 'https://example.com/city-static.mp3', '11111111-1111-4111-8111-111111111115'::uuid),
        ('22222222-2222-4222-8222-222222222211'::uuid, 'Red Horizon', 'Nova Echo', 'https://example.com/red-horizon.mp3', '11111111-1111-4111-8111-111111111115'::uuid),
        ('22222222-2222-4222-8222-222222222212'::uuid, 'Last Train Home', 'Nova Echo', 'https://example.com/last-train-home.mp3', '11111111-1111-4111-8111-111111111115'::uuid),
        ('22222222-2222-4222-8222-222222222213'::uuid, 'Still Water', 'Atlas Bloom', 'https://example.com/still-water.mp3', '11111111-1111-4111-8111-111111111116'::uuid),
        ('22222222-2222-4222-8222-222222222214'::uuid, 'Tidal Memory', 'Atlas Bloom', 'https://example.com/tidal-memory.mp3', '11111111-1111-4111-8111-111111111116'::uuid),
        ('22222222-2222-4222-8222-222222222215'::uuid, 'Driftwood', 'Atlas Bloom', 'https://example.com/driftwood.mp3', '11111111-1111-4111-8111-111111111116'::uuid),
        ('22222222-2222-4222-8222-222222222216'::uuid, 'Neon River', 'Luna Park', 'https://example.com/neon-river.mp3', '11111111-1111-4111-8111-111111111117'::uuid),
        ('22222222-2222-4222-8222-222222222217'::uuid, 'Electric Bloom', 'Luna Park', 'https://example.com/electric-bloom.mp3', '11111111-1111-4111-8111-111111111117'::uuid),
        ('22222222-2222-4222-8222-222222222218'::uuid, 'Afterglow Arcade', 'Luna Park', 'https://example.com/afterglow-arcade.mp3', '11111111-1111-4111-8111-111111111117'::uuid),
        ('22222222-2222-4222-8222-222222222219'::uuid, 'Small Things', 'Mira Lane', 'https://example.com/small-things.mp3', '11111111-1111-4111-8111-111111111118'::uuid),
        ('22222222-2222-4222-8222-222222222220'::uuid, 'Northbound', 'Mira Lane', 'https://example.com/northbound.mp3', '11111111-1111-4111-8111-111111111118'::uuid),
        ('22222222-2222-4222-8222-222222222221'::uuid, 'Test Song One', 'Test Artist', 'https://example.com/test-song-one.mp3', '11111111-1111-4111-8111-111111111111'::uuid),
        ('22222222-2222-4222-8222-222222222222'::uuid, 'Test Song Two', 'Test Artist', 'https://example.com/test-song-two.mp3', '11111111-1111-4111-8111-111111111111'::uuid),
        ('22222222-2222-4222-8222-222222222223'::uuid, 'A Quiet Beginning', 'Atlas Bloom', 'https://example.com/a-quiet-beginning.mp3', '11111111-1111-4111-8111-111111111116'::uuid),
        ('22222222-2222-4222-8222-222222222224'::uuid, 'Midnight Runner', 'Luna Park', 'https://example.com/midnight-runner.mp3', '11111111-1111-4111-8111-111111111117'::uuid)
)
INSERT INTO song (public_id, created_at, updated_at, title, artist, audio_url, album_id)
SELECT d.public_id, NOW(), NOW(), d.title, d.artist, d.audio_url, a.id
FROM song_data d
         JOIN album a ON a.public_id = d.album_uuid
    ON CONFLICT (public_id) DO UPDATE SET
    title = EXCLUDED.title,
                                   artist = EXCLUDED.artist,
                                   audio_url = EXCLUDED.audio_url,
                                   album_id = EXCLUDED.album_id,
                                   updated_at = NOW();

COMMIT;

-- Summary counts
SELECT 'albums' AS entity, COUNT(*) AS total FROM album
UNION ALL
SELECT 'songs', COUNT(*) FROM song;
