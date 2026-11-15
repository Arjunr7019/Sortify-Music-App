Public response samples from YouTube Music `/search` (Arijit Singh songs and
Hindi music playlists) and `/browse` (India charts), captured 2026-11-15 using
`scripts/probe_youtube_music.py --fixtures app/src/test/resources/youtube`.
Two result renderers per response are retained. Tracking parameters, menus,
accessibility labels, and overlays are removed. Metadata and artwork URLs remain
unaltered. These files are parser test inputs, not production catalog assets.
