# Sortify

A glassmorphism-styled React Native (Expo) music player built on top of the
Saavn wrapper API (`https://saavn.sumit.co/api`). Every network call is a
`GET` request, as documented in `music_api.txt`.

## Getting started

```bash
npm install
npx expo start
```

Requires Node 18+ and the Expo Go app (or a dev build) on your phone, or an
Android/iOS simulator. This project uses the Expo managed workflow, so no
native Xcode/Android Studio setup is required for day-to-day development.

> Package versions in `package.json` target Expo SDK 51. If Metro complains
> about mismatched versions after `npm install`, run `npx expo install
> --fix` once to align everything to your local Expo CLI/SDK.

## What's inside

- **Onboarding** — first launch asks which languages you listen to
  (Kannada, English, Hindi, Tamil, Telugu, Marathi, Malayalam). The choice
  is saved locally and drives the "Trending in <language>" rows on Home.
- **Home** — Last session (last 12 played songs, horizontal scroll), Your
  playlists (name + song count), and one trending row per language you
  selected during onboarding.
- **Search** — a single search bar that queries songs, albums, and artists
  together (via the global `/search` endpoint) with debounced typing.
- **Library** — Favorites, Playlist, and Download hubs.
- **Floating player** — a mini bar sits above the tab bar whenever a song
  is playing. Tap it or swipe up to expand into the full player card
  (banner, progress bar, previous/play-pause/next, favorite, add-to-playlist,
  download). Swipe down (or the chevron) collapses it again.
- **Offline downloads** — the download button saves the audio file locally
  with `expo-file-system`; the Downloads tab plays from the local file so it
  works without a network connection.

## Design

Colors are pulled straight from the logo gradient (pink → coral → amber) on
a near-black background matching `icon.png`/`adaptive-icon.png`. Every card,
the search bar, the tab bar, and the player use `expo-blur` + soft borders
for the glassmorphism look, with the gradient reserved for primary actions
(play button, active tab accents, playlist artwork, continue button).

## API notes

The reference API has no dedicated "trending" or "trending by language"
endpoint, so those rows are built from `/search/songs` with a
language-targeted query (e.g. "Kannada hit songs"). Everything else maps
directly to the documented endpoints: global search, search by
song/album/artist/playlist, song by ID, song suggestions, and artist
lookup/songs/albums.

## Project structure

```
App.js
src/
  api/musicApi.js        API client + normalizeSong()
  context/                Player, Library (favorites/playlists/downloads), Onboarding
  components/              GlassView, SongCard, SongListRow, PlaylistCard,
                            PlayerSheet (mini + expanded player), AddToPlaylistModal,
                            AppBackground
  navigation/              Bottom tabs + glass tab bar + library stack
  screens/                 Home, Search, Library hub, Favorites, Playlists,
                            PlaylistDetail, Downloads, LanguageSetup
  theme/colors.js
  utils/                   format.js (mm:ss), download.js (expo-file-system)
```
