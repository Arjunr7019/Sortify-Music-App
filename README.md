# Sortify for Android

A native Kotlin / Jetpack Compose rebuild of the supplied Sortify React Native app. It uses the supplied green Sortify mark and extends the forest-green music UI reference across the full app. No account is required.

See the [emulator screenshots](docs/screenshots/README.md) and [verification record](docs/VERIFICATION.md).

## Open and run

1. Open **this directory** in Android Studio.
2. Install Android SDK Platform 36 and use JDK 17.
3. Let Gradle sync, then run the `app` configuration on Android 8.0 / API 26 or newer.

From PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

The Gradle wrapper is included. Android Studio normally writes `local.properties` with your SDK location; this machine-specific file is excluded from the source package.

## APK releases

The [branch release workflow](.github/workflows/android-release.yml) builds the
native app from `sortify-kotlin` and the Expo app from `main`. Branch pushes
produce downloadable Actions artifacts; tags such as `kotlin-v2.0.0` and
`expo-v1.7.0` publish separately named GitHub releases. No
`ANDROID_KEYSTORE_BASE64` or Expo token is required. APKs use debug signing for
sideloading, including the release build type.

See [release setup and commands](docs/RELEASES.md) for installing the workflow on
both branches, publishing tags, and signing/update limitations.

## Screens and interactions

- **Welcome and language setup:** no sign-in; choose one or more music languages.
- **Home:** mixes, language-based discovery, artist search, your playlists, and recent listening.
- **Search:** debounced song, artist, album, and playlist search; browse moods; drill into catalog results; empty, loading, and retry states.
- **Library:** liked songs, personal playlists, downloads, and recent songs; filter and sort playlists.
- **Create:** name a playlist and optionally start with selected tracks. Cover artwork follows its contents.
- **Playlist details:** play, shuffle, save catalog mixes to your library, download, share, rename, delete, and remove tracks.
- **Player:** real audio streaming, seek, previous/next, shuffle, repeat all/one, favourites, download, sleep timer, and queue.
- **Queue:** play a queued track, move tracks earlier, and remove tracks.
- **Settings:** dark/light/system appearance, 96/160/320 kbps quality, languages, sleep timer, download management, and app information.
- Persistent mini-player, Android system Back, edge-to-edge insets, and navigation rail on wider layouts.

`MediaSessionService` hosts Media3 ExoPlayer separately from the activity for background playback, audio focus, headphone unplug handling, and system media controls. The app requests notification permission when playback first starts.

Likes, playlists, language preferences, quality, appearance, recent listening, and download records persist in app-private storage. Audio downloads are written to app-private files using temporary files and atomic rename; already-downloaded audio is preferred for playback. Sharing sends the actual song or playlist text through the Android sharesheet.

## Music service

The original ZIP used `https://saavn.sumit.co/api`. During implementation that URL returned HTML instead of the expected JSON. This build uses the compatible endpoint configured in [app/build.gradle.kts](app/build.gradle.kts):

```kotlin
buildConfigField("String", "MUSIC_API_BASE", "\"https://saavn.bycomsolutions.com/api\"")
```

Change this value to your own compatible API and rebuild. Contract documentation: [compatible service API](https://saavn.bycomsolutions.com/docs), [original API source](https://github.com/sumitkolhe/jiosaavn-api).

Discovery uses language-targeted song searches, as in the original app; these are not official chart rankings. Playback, searches, catalog pages, and new downloads require an available music service and accessible audio URLs. Network failures show an actionable error. Unavailable tracks are not simulated.

A small catalog snapshot and original cover artwork are bundled for first-launch browsing and offline visual continuity. **No music audio is bundled**, and a displayed catalog entry is not an offline download. The snapshot lives in `app/src/main/assets/catalog.json`; each artwork URL is recorded in `provenance.json`. Original user design/logo files are preserved in `docs/references/`; the app mark is a vector recreation of the supplied mark, not a generated replacement logo.

To refresh the snapshot and artwork on a normally configured development machine:

```powershell
python scripts/prepare_assets.py
```

The optional `--insecure-proxy` argument is solely for the local development proxy used while preparing public metadata assets. It is never used by Android; the app retains standard HTTPS certificate validation.

## Validate

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

Playback policy tests cover bitrate fallback, exact quality, offline preference, insecure/absent streams, and duration formatting. `scripts/ui_smoke.py` drives the installed app through Android accessibility nodes and captures original emulator screenshots. Run it on a dedicated emulator with serial `emulator-5554`; its test playlist persists. See [docs/VERIFICATION.md](docs/VERIFICATION.md) for the recorded runtime checks and limitations.

## Project layout

```text
app/src/main/java/com/sortify/app/
  MainActivity.kt               Adaptive app shell and navigation
  SortifyViewModel.kt           UI state, library actions, queue, MediaController
  data/                        Models, API, local preferences, downloads
  playback/PlaybackService.kt   Media3 playback and media session
  ui/                          Theme, artwork loader, reusable controls, screens
app/src/main/assets/            Browse catalog, original artwork, provenance
app/src/test/                   Playback policy tests
docs/                          Reference assets, screenshots, verification
Sortify-Music-App-main/         Supplied React Native source, kept as reference
```

Build tool versions are deliberately pinned to the installed compatible toolchain. Android lint may report newer dependency versions and advisory warnings; the recorded verification distinguishes warnings from build errors. Debug and release APKs use debug signing for sideloading; a Play Store release needs your private signing configuration.
