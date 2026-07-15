# Sortify

A React Native (Expo) music player built on top of the Saavn wrapper API
(`https://saavn.sumit.co/api`). Every network call is a `GET` request, as
documented in `music_api.txt`.

## Getting started

```bash
npm install
npx expo start
```

Requires Node 18+ and the Expo Go app (or a dev build), or an Android/iOS
simulator. Managed Expo workflow — no native Xcode/Android Studio setup
needed for day-to-day dev.

> If Metro complains about mismatched versions after `npm install`, run
> `npx expo install --fix` once to align everything to your local Expo
> CLI/SDK.

## What's inside

- **Onboarding** — first launch asks which languages you listen to
  (Kannada, English, Hindi, Tamil, Telugu, Marathi, Malayalam). Drives the
  "Trending Songs" rows on Home. Editable later from Settings.
- **Home** — Last Session (last 12 played, horizontal scroll), Your
  Playlists (2x2 thumbnail collage + song count), one Trending Songs row
  per language you selected.
- **Search** — single search bar over songs/albums/artists via the global
  `/search` endpoint, debounced.
- **Library** — Favourites, Playlists, Downloads.
- **Floating player** — a mini bar with a thin progress line sits above the
  tab bar whenever something is playing. Tap or swipe up to expand into
  the full player (banner, progress, prev/play-pause/next, favourite,
  add-to-playlist, download). Swipe down or the chevron collapses it.
- **Offline downloads** — saved locally with `expo-file-system`; the
  Downloads tab plays from disk.
- **Settings** — tap the menu icon (top-left on Home/Search/Library) to
  open Settings: a Light/Dark mode switch (persisted, defaults to the
  device's system theme) and your selected trending languages.

## Design

Flat light/dark design system (`src/theme/palette.js` + `ThemeContext`) —
solid surfaces, a single accent orange, no blur/glass effects. Every
screen and component reads its colors from `useAppTheme()`, so the whole
app (including the floating player and every modal) reacts instantly to
the Settings toggle.

## API notes

The reference API has no dedicated "trending" endpoint, so those rows are
built from `/search/songs` with a language-targeted query (e.g. "Kannada
hit songs"). Everything else maps directly to documented endpoints:
global search, search by song/album/artist/playlist, song by ID, song
suggestions, artist lookup/songs/albums.

## Project structure

```
App.js
src/
  api/musicApi.js         API client + normalizeSong()
  context/                 Theme (light/dark + settings modal), Player,
                            Library (favorites/playlists/downloads), Onboarding
  components/               AppScreen, Header (menu + avatar), SongCard,
                            SongListRow, PlaylistCard (thumbnail collage),
                            PlayerSheet (mini + expanded player), AddToPlaylistModal
  navigation/                Bottom tabs + flat tab bar + library stack
  screens/                   Home, Search, Library hub, Favorites, Playlists,
                            PlaylistDetail, Downloads, LanguageSetup, Settings
  theme/palette.js           Light + dark color palettes
  utils/                    format.js (mm:ss), download.js (expo-file-system)
```
