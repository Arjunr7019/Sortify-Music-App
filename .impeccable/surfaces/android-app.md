# Sortify Android

Mode: Operate. Native music discovery and playback, extended from the user's supplied phone reference.

## Direction contract
THESIS: Artwork leads discovery; one persistent player joins browsing, search, and a personal library.
OWN-WORLD: Near-black forest surfaces, white type, vivid Sortify green, outlined pill filters, softly rounded square art, circular artists, and green active-track rows.
STORY: Enter without an account, choose languages, find a track, play it, then collect it in a personal library.
FIRST VIEWPORT: Compact brand/greeting row, a large two-line welcome, horizontal filters, artwork-led recommendations, then favourite artists. The mini-player sits directly above native navigation. Playlist detail gives artwork the upper third and keeps play prominent.
FORM: Native Compose app screens; seed: none (pinned). User: "change the design that is given in first image and other images are new logo". Extend the same world to search, library, player, queue, creation, downloads, and settings. App screens use code-first for this session; no standing workflow preference is recorded.
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance

## Player interaction

Tap or swipe up on the persistent mini-player to expand an overlay above the retained browsing screen. Drag down from the full-player header or artwork to collapse; keep seeking and transport controls outside that gesture area. Motion follows the finger, then settles with FastOutSlowIn easing (320ms entry, 240ms exit), respecting Android animation duration scale. Short drags return to the starting state; horizontal drags suppress accidental taps. Preserve playback, browsing scroll position, search query, queue navigation, and system Back. Clear search focus when opening the overlay.
