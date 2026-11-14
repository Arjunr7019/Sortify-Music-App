# Verification record

Verified on 14 November 2026 using the installed Android 15 / API 35 emulator. This record describes executed checks, not coverage claims for every Android device.

## Build checks

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

- APK compilation and packaging: passed.
- APK signing check: `apksigner verify --verbose` passed using APK Signature Scheme v2. Manifest inspection confirms `com.sortify.app`, version 1.0.0, minimum SDK 26, target SDK 35.
- Unit tests: **5 passed, 0 failures, 0 errors**.
- Android lint: **0 errors, 19 warnings, 5 hints**. Advisories include available dependency updates, the selected target SDK, media-service export, backup configuration, and Kotlin extension suggestions. No lint baseline or error suppression was used to pass the checks.
- Runtime logs: no app `AndroidRuntime` crashes or ExoPlayer playback errors during the recorded flows.
- All 16 bundled cover-art files have embedded origin metadata and entries in `app/src/main/assets/provenance.json`.

## Executed device flows

| Flow | Evidence / result |
| --- | --- |
| First launch | Welcome → multi-select language setup → home; no account required. |
| Discovery | Live language-targeted searches returned artwork and songs. |
| Song search | Search for “Blinding Lights” returned playable song rows. |
| Catalog search | “The Weeknd” artist results opened an artist page with 10 tracks; album results also loaded. |
| Personal playlist | Created “The long way home” with two selected songs; detail and library show the saved playlist. |
| Favourites | Added a song through its options; the liked collection and persisted preference contain it. |
| Streaming | Media3 decoded and advanced a remote stream; the Android media session reported `PLAYING` and advancing position. |
| Background playback | Sent app to system Home; Sortify’s media session remained `PLAYING`. |
| Download | Saved a 4,143,263-byte audio file to app-private storage and persisted its download record. |
| Offline playback | Disabled both Wi-Fi and mobile data; downloaded audio played and continued in the background. Network settings were restored afterward. |
| Error recovery | Offline search showed connection error and Retry. Restoring connectivity and retrying completed the search with the normal empty-result state. |
| Persistence | Force-stopped and reopened app; onboarding, playlist, favourite, download, recent listening, quality, and appearance remained saved. |
| Themes and quality | Changed 160 → 320 kbps; switched light/dark; inspected quality selection dialog and light settings. |
| Layout | Captured phone, 130% font-scale home/library, and wide-layout home/library with a navigation rail. Restored resolution, density, night mode, and font scale afterward. |

## Capture environment

Phone: `Medium_Phone_API_35`, 1080 × 2400 px, density 420. Wide layout: the same emulator with a 1600 × 2560 px / density 240 override; this checks adaptive layout, not physical tablet behavior. Font-size check uses Android’s system `font_scale=1.3`.

Final deterministic captures disable the animator duration scale, because the headless software renderer intermittently captured partial navigation animations. The original animator setting is restored after capture. Screenshots are direct `adb exec-out screencap -p` output; no composited mockups or browser renders are presented as app evidence.

An additional launch after restoring normal animations showed all four navigation destinations correctly. The [design review](DESIGN_REVIEW.md) records the caption-clipping correction and its resolved verdict.

## Practical limits

- No physical device, API 26 device, headset, Bluetooth device, or lock-screen gesture was available for hardware validation.
- Media-session background playback was observed; audible output was not evaluated because the emulator was launched without audio.
- Streaming/catalog availability depends on the external compatible music API and audio hosts. Offline playback requires completing an actual download.
- The distributable is signed with the development debug key. Store publishing and release signing are not configured.

## Screenshots

The `screenshots/` directory contains selected original emulator captures. The screenshot names describe their screen/state; the tablet images are resolution-override captures as described above. The fuller working capture set and accessibility trees live in the local `.impeccable/review/` directory and are excluded from the source ZIP.
