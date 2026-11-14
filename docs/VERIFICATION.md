# Verification record

Verified on 14 November 2026 using the installed Android 15 / API 35 emulator. This record describes executed checks, not coverage claims for every Android device.

## Build checks

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

- APK compilation and packaging: passed.
- APK signing check: `apksigner verify --verbose` passed using APK Signature Scheme v2. Manifest inspection confirms `com.sortify.app`, version 2.0.1 (version code 2), minimum SDK 26, target SDK 35.
- Unit tests: **10 passed, 0 failures, 0 errors** (five playback policy tests and five player gesture policy tests).
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

## Player gesture update (2.0.1)

Built and installed the updated debug APK on 14 November 2026. `scripts/verify_player_gestures.py` checks the player interactions on the dedicated `emulator-5554`; complete onboarding first. The source document described version 1.0.1, but this repository was already at 2.0.0, so this update uses 2.0.1.

- Short upward and horizontal mini-player drags keep it collapsed; taps and deliberate upward swipes expand it.
- Short downward drags keep the full player open; deliberate drags on either the header or artwork collapse it.
- Visible playlist text and bounds matched before and after expanding/collapsing. Android's media session stayed `PLAYING` with advancing position.
- Seeking, mini-player play/pause, queue navigation, and system Back passed. Back from Queue returns to Player, then to the original browsing screen.
- Gestures also passed with `animator_duration_scale=0`; the previous setting was restored afterward.
- Opening the player from a focused Search field cleared focus and dismissed the keyboard. Collapsing retained the `Blinding Lights` query without refocusing the field.
- The player expanded at 130% system font scale and expanded/collapsed at the 1600 ? 2560 / density 240 wide-layout override. Inspected the direct captures; all settings were restored afterward.
- The expanded player's accessibility tree omitted the covered Home and library navigation controls.
- The original [gesture recording](player-gestures.mp4) and [resting captures](screenshots/README.md) were captured directly from the emulator. Playback continuity is based on media-session state, not an audible-output claim.

The build, unit tests, Android lint, and APK signature verification passed. Lint still reports 19 warnings and five hints. An initial build encountered a Windows lock on generated `R.jar`; a retry using a fresh Gradle daemon completed successfully.

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
