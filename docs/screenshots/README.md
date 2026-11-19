# Sortify screen previews

Original captures from the Android API 35 emulator. See [verification details](../VERIFICATION.md) for device dimensions, font scale, and the tablet-size override.

| Screen | Capture |
| --- | --- |
| Welcome | [welcome-phone.png](welcome-phone.png) |
| Language setup | [languages-phone.png](languages-phone.png) |
| Home | [home-phone.png](home-phone.png) |
| Playlist | [playlist-phone.png](playlist-phone.png) |
| Full player (earlier baseline) | [player-phone.png](player-phone.png) |
| Player expanded by swipe (2.0.1) | [gesture-expanded-phone.png](gesture-expanded-phone.png) |
| Playlist before expansion (2.0.1) | [gesture-browse-before-phone.png](gesture-browse-before-phone.png) |
| Playlist after collapse (2.0.1) | [gesture-collapsed-phone.png](gesture-collapsed-phone.png) |
| Search retained after collapse (2.0.1) | [gesture-search-preserved-phone.png](gesture-search-preserved-phone.png) |
| Player at 130% text size (2.0.1) | [gesture-player-font130-phone.png](gesture-player-font130-phone.png) |
| Player on wide layout (2.0.1) | [gesture-player-tablet.png](gesture-player-tablet.png) |
| Wide layout after collapse (2.0.1) | [gesture-collapsed-tablet.png](gesture-collapsed-tablet.png) |
| Play queue | [queue-phone.png](queue-phone.png) |
| Search results | [search-results-phone.png](search-results-phone.png) |
| Library | [library-phone.png](library-phone.png) |
| Create playlist | [create-empty-phone.png](create-empty-phone.png) |
| Downloads | [downloads-phone.png](downloads-phone.png) |
| Settings | [settings-phone.png](settings-phone.png) |
| Light appearance | [settings-light-phone.png](settings-light-phone.png) |
| Audio quality | [quality-phone.png](quality-phone.png) |
| Offline search error | [search-offline-phone.png](search-offline-phone.png) |
| Enlarged text | [home-font130-phone.png](home-font130-phone.png) |
| Wide layout | [home-tablet.png](home-tablet.png) |
| Skeleton loading: Home | [skeleton-home-phone.png](skeleton-home-phone.png) |
| Skeleton loading: Search | [skeleton-search-phone.png](skeleton-search-phone.png) |
| Skeleton loading: light appearance | [skeleton-home-light-phone.png](skeleton-home-light-phone.png) |
| Skeleton loading: enlarged text | [skeleton-home-font130-phone.png](skeleton-home-font130-phone.png) |
| Skeleton loading: wide layout | [skeleton-home-tablet.png](skeleton-home-tablet.png) |

These are live app screens with a test library, real catalog metadata, and actual playback state. They are not generated mockups. Original pixels are preserved. The 2.0.1 gesture captures come directly from `adb exec-out screencap -p`; their origin and hashes are recorded in [gesture-provenance.json](gesture-provenance.json). The original [gesture recording](../player-gestures.mp4) shows expansion and collapse in motion.

The skeleton captures were taken on 19 November 2026 with requests deliberately held by a temporary emulator proxy. Animation duration scale was set to zero for deterministic captures and to verify that placeholders remain visible with Remove animations. The proxy, duration scale, font scale, resolution, density, and app appearance were restored after verification.
