# YouTube Music integration

The native Android app supports two search providers: JioSaavn and YouTube Music.
The selector below Search retains the query and category and saves the preferred
provider on the device. Songs, artists, albums, and playlists use the selected
provider. Switching provider cancels the previous search and clears its results;
late responses cannot overwrite the new provider's results.

Home includes **Trending on YouTube Music**, sourced from YouTube Music's India
charts, and **YouTube Music playlists**, using playlist searches for each
language selected in Sortify's existing language settings. Language chips select
which shelf is shown. Featured playlists are preferred, with community results
when there are few matches. Supplemental results without the requested language
in their title are excluded. These searches are discovery recommendations, not
an assertion that every track in a playlist has that language. Charts, language
playlists, and JioSaavn discovery load independently and expose retry states.

## API and playback

`YouTubeMusicClient.kt` adapts the supplied ytmusicapi source to Kotlin, using
`POST /search` and `POST /browse` under
`https://music.youtube.com/youtubei/v1/`. The base URL is configured by
`YOUTUBE_MUSIC_API_BASE` in `app/build.gradle.kts`. No Python service, API key,
Google login, browser cookie, or account sync is required.

YouTube Music's internal `/player` can return ciphered streams without direct
audio URLs. The integration therefore uses NewPipeExtractor v0.27.1 for public
audio extraction, then feeds progressive HTTPS audio into Media3. Media3 resolves
each YouTube track on its IO thread when opening it; queue entries hold stable
video IDs rather than expiring stream URLs. A five-minute memory cache avoids
repeated extraction during seeks. The closest available bitrate is chosen;
YouTube does not necessarily offer the exact 96/160/320 kbps selected in Settings.

Source identity is stored in songs saved to likes, personal playlists, recent
listening, and downloads. YouTube IDs use the `yt:` prefix to avoid collisions.
Older saved songs without a provider retain JioSaavn behavior. YouTube and
JioSaavn songs can share the same queue and personal playlist. Existing downloaded
files take priority over streaming. Downloads use the same resolver as playback.

Private/account operations listed in the supplied reference (uploads, Google
library changes, subscriptions) are outside this integration. Sortify's personal
library remains local. Region restrictions, sign-in requirements, throttling,
and upstream API changes may make individual tracks unavailable; these produce
real error states and are never replaced with a track from another provider.

## Dependencies and attribution

- [NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor/tree/v0.27.1),
  Team NewPipe, GPL-3.0-or-later. The license is bundled in
  `app/src/main/assets/licenses/NewPipeExtractor-GPL-3.0.txt`. This dependency
  introduces GPL distribution obligations; retain its license and provide the
  corresponding source under the applicable terms when distributing the app.
- [ytmusicapi](https://github.com/sigma67/ytmusicapi), supplied source ZIP and
  endpoint/method references: request contexts, filter parameters, and public
  renderer structure informed the native adapter. Its supplied license is
  preserved at `app/src/main/assets/licenses/ytmusicapi-LICENSE.txt`.
- JitPack repository access is restricted to the two NewPipe dependency groups.
  Core-library NIO desugaring supports the app's existing Android 8+ minimum.

## Verification

Run `gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.
The six new contract tests cover captured live song/playlist/chart renderers,
artwork, duration, source identity, invalid entries, deduplication, and legacy
provider defaults. Existing playback and gesture tests remain in the same suite.

`python scripts/probe_youtube_music.py` probes public metadata endpoints.
`--playback` demonstrates the supplied library's raw `/player` limitation;
it does **not** exercise NewPipeExtractor. Actual playback is checked in Android.
`scripts/verify_youtube_music.py` drives the dedicated `emulator-5554` using
accessibility selectors and captures screenshots under `.impeccable/review/`.
Its `matrix` phase restores font scale, screen size, and density in `finally`.

Live metadata test fixtures are under `app/src/test/resources/youtube/`.
They contain only public renderer data needed for parsing, with action menus,
tracking fields, and overlays removed. They are test resources and do not ship
as a pretend offline YouTube catalog.

### Recorded checks — 2026-11-15

- Debug and release APK builds passed. All 16 unit tests passed (six new parser
  contract tests plus the ten existing playback/gesture tests). Android lint
  completed with zero errors, 22 warnings, and five hints.
- Public song search, playlist search, and India chart requests returned live
  results. Opening a YouTube playlist loaded 25 songs on the test run.
- A YouTube song played through Media3 with a 20-track queue. The Android media
  session reported PLAYING with advancing position and buffered audio. Playback
  continued after sending the app to the background.
- A YouTube song was liked and downloaded; its persisted JSON retained
  `provider=YouTubeMusic` and a local file URI. The downloaded song played with
  Wi-Fi and mobile data disabled. Network settings were restored afterward.
- Switching to JioSaavn preserved the query/category and loaded JioSaavn results.
  Changing the Home language chip displayed the corresponding playlist shelf.
- YouTube search displayed an error with Retry while networking was disabled,
  and recovered with live results after reconnecting and pressing Retry.
- Visual captures used the dedicated API 35 emulator: 1080×2400 at 420 dpi,
  font scales 1.0 and 1.3, and an expanded 1600×2560 layout at 240 dpi on the same
  emulator. Font scale, screen size, and density were restored. No physical
  device or Android 8 runtime was tested.
- The independent native finish review returned **ship**, with no material UI
  fixes, after reviewing twelve captures and the original reference. The existing
  design system was preserved. Static captures do not establish TalkBack behavior
  or hardware performance.

Installable build: `app/build/outputs/apk/release/app-release.apk` (existing
debug-key sideload signing). Development build:
`app/build/outputs/apk/debug/app-debug.apk`.
The release APK passed `apksigner verify` with APK Signature Scheme v2.

Emulator screenshots: [Home](screenshots/youtube-home-phone.png),
[Hindi playlists](screenshots/youtube-home-hindi-phone.png),
[YouTube search](screenshots/youtube-search-phone.png),
[expanded Home](screenshots/youtube-home-tablet.png). These are unmodified device
captures of the final debug build; covers come from the public API responses.
