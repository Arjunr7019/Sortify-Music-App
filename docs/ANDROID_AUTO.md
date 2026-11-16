# Android Auto

Sortify exposes its existing player through a Media3 `MediaLibraryService`. Android Auto supplies the car interface. This integration targets phone projection through Android Auto; a separate Android Automotive OS application is not included.

## What is available

- Four browse destinations: Home (recent listening and bundled catalog), Liked songs, Downloads, and saved Playlists.
- Selecting a song queues its collection in saved order and starts at the selected song. The player uses canonical song IDs while browse IDs retain their collection, including YouTube Music and playlist IDs containing special characters.
- Play, pause, seek, previous/next, shuffle, and repeat use Media3's standard player commands.
- Voice playback and library search match saved titles, artists, and albums first. With no saved matches, search uses the provider currently selected on the phone. A request without search words uses Home.
- Downloaded files are preferred for both providers. Missing files fall back to streaming where a stream is available. YouTube streams use the existing deferred resolver and the saved quality setting.
- The service saves recent listening without an activity. Phone queue and song metadata update when car playback changes, including when the phone already has a different queue.
- Phone library edits notify subscribed car browsers. The service can start before onboarding or an activity; it requires no sign-in.

The manifest declares the automotive media capability, Media3 library/session service actions, the legacy media-browser action, and a voice-search activity entry. `automotive_app_desc.xml` declares the `media` capability. No extra car UI library is required.

## Install and try

```powershell
.\gradlew.bat :app:assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Open Sortify on the phone, optionally save songs or playlists, then connect to Android Auto and choose Sortify in its launcher. The debug-signed sideload build may require **Unknown sources** in Android Auto developer settings:

1. Open Android Auto settings on the phone.
2. Tap its version/permission information ten times and accept developer mode.
3. Open Developer settings from the overflow menu and enable Unknown sources.
4. Reconnect Android Auto; check Customize launcher if Sortify is hidden.

Google documents the [Android Auto media declaration](https://developer.android.com/training/cars/media/auto), [Media3 library service](https://developer.android.com/media/media3/session/serve-content), and [sideload testing and startup scenarios](https://developer.android.com/training/cars/testing).

## Regression checks

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

`CarLibraryTest` checks folder and playlist organization, recent-first deduplication, contextual IDs, paging overflow, downloaded-file preference and fallback, and deferred YouTube resolution.

`AndroidAutoPlaybackTest` uses an Android emulator, the platform legacy `MediaBrowser`/`MediaController`, and a Media3 browser. It generates a short silent WAV locally, temporarily seeds library preferences, and restores the changed preferences afterward. It checks service startup without a phone activity, folders, local playback, collection queues, pause/skip, recent history, updated favorites, voice playback, and replacement of a phone player's existing queue.

Verified on 16 November 2026: debug APK built successfully; all 30 unit tests and both integration tests passed on the Android 15 / API 35 emulator. Android lint passed with 0 errors, 17 warnings, and 5 hints. No lint baseline or error suppression was added.

These protocol tests do not cover physical car hardware, the Android Auto launcher/UI, microphone recognition, audible output, live provider availability, or distribution review. Verify those separately with the Desktop Head Unit or a real car before publishing. Current APKs use the repository's development signing configuration.
