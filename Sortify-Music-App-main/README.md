# Sortify

A React Native (Expo) music player built on top of the Saavn wrapper API
(`https://saavn.sumit.co/api`). Every network call is a `GET` request, as
documented in `music_api.txt`.

## ⚠️ Requires a custom dev build — not plain Expo Go

Background/lock-screen playback needs `expo-audio`'s background-playback
config plugin, which bakes native settings into your app that Expo Go can't
apply per-project. One-time setup:

```bash
npm install
npx expo prebuild          # generates the ios/ and android/ native projects
npx expo run:android       # or: npx expo run:ios
```

After that, `npx expo start` + the dev client you just built works like
Expo Go did. Building an installable version for others (or for
TestFlight/Play Console) is done with [EAS Build](https://docs.expo.dev/build/introduction/)
instead of `expo run:*`.

> This project targets Expo SDK 56. If Metro complains about mismatched
> versions after `npm install`, run `npx expo install --fix` once to align
> every package to what your installed Expo CLI/SDK actually expects —
> the exact versions pinned here are a best-effort starting point, not
> gospel.

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
- **Floating in-app player** — a mini bar with a thin progress line sits
  above the tab bar whenever something is playing. Tap or swipe up to
  expand into the full player. Swipe down or the chevron collapses it.
  Previous/next, favorite, and add-to-playlist all live here.
- **Background / lock-screen playback** — powered by `expo-audio`
  (first-party Expo module, no third-party native dependency). While a
  song is playing, the lock screen and notification shade show artwork,
  title/artist, a progress bar, and play/pause + seek forward/backward,
  and audio keeps playing while the app is backgrounded or in Recents.
  **Known gap:** expo-audio's lock-screen API doesn't currently expose a
  "skip to next/previous track" button or a "like/heart" button on the OS
  lock screen itself — those controls only exist in-app (mini player and
  full player). If that becomes a dealbreaker later, `react-native-track-player`
  supports both, at the cost of the native-dependency fragility and (for
  v5) licensing considerations discussed when this decision was made.
- **Audio quality** — Settings → Audio quality lets you pick 96/160/320kbps,
  matching the bitrates the API actually returns per song
  (`downloadUrl[].quality`). Falls back to the closest available bitrate
  if a track doesn't have your exact choice.
- **Offline downloads** — saved locally with `expo-file-system`; the
  Downloads tab plays from disk regardless of your quality setting.
- **Settings** — tap the menu icon (top-left on Home/Search/Library) to
  open Settings: Light/Dark mode (persisted, defaults to the device's
  system theme), audio quality, and your selected trending languages.

## Design

Flat light/dark design system (`src/theme/palette.js` + `ThemeContext`) —
solid surfaces, a single accent orange, no blur/glass effects. Every
screen and component reads its colors from `useAppTheme()`.

## API notes

The reference API has no dedicated "trending" endpoint, so those rows are
built from `/search/songs` with a language-targeted query (e.g. "Kannada
hit songs"). Everything else maps directly to documented endpoints:
global search, search by song/album/artist/playlist, song by ID, song
suggestions, artist lookup/songs/albums. Quality selection reads
`downloadUrl[].quality`/`.url` directly off each song.

## Project structure

```
App.js
src/
  api/musicApi.js          API client, normalizeSong(), resolveAudioUrl() for quality
  context/                  Theme (light/dark + settings modal), Player (expo-audio),
                            Quality (streaming bitrate), Library, Onboarding
  components/                AppScreen, Header (menu + avatar), SongCard, SongListRow,
                            PlaylistCard, PlayerSheet (mini + expanded player),
                            AddToPlaylistModal, LanguagePickerModal
  navigation/                 Bottom tabs + flat tab bar + library stack
  screens/                    Home, Search, Library hub, Favorites, Playlists,
                            PlaylistDetail, Downloads, LanguageSetup, Settings
  theme/palette.js             Light + dark color palettes
  utils/                      format.js (mm:ss), download.js (expo-file-system)
```
