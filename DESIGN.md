---
name: Sortify
description: Native Android music discovery and playback in the supplied forest-green visual world.
colors:
  sortify-green: "#1ED76A"
  dark-primary: "#1ED76A"
  dark-on-primary: "#00210D"
  dark-primary-container: "#164B31"
  dark-on-primary-container: "#D0FFDD"
  dark-background: "#00160D"
  dark-on-background: "#F5FAF6"
  dark-surface: "#091F16"
  dark-on-surface: "#F5FAF6"
  dark-surface-variant: "#20352A"
  dark-on-surface-variant: "#B8C8BE"
  dark-secondary-container: "#2A3D32"
  dark-on-secondary-container: "#E1F2E7"
  dark-outline: "#728A7C"
  dark-outline-variant: "#344D3F"
  dark-error: "#FFB4AB"
  dark-on-error: "#690005"
  dark-inverse-surface: "#DCECE0"
  dark-inverse-on-surface: "#102C1B"
  dark-inverse-primary: "#007B36"
  dark-secondary: "#A1D8B6"
  dark-on-secondary: "#073A20"
  dark-surface-tint: "#1ED76A"
  dark-surface-container-lowest: "#001108"
  dark-surface-container-low: "#0D2419"
  dark-surface-container: "#10291D"
  dark-surface-container-high: "#1B3427"
  dark-surface-container-highest: "#233E30"
  light-primary: "#007B36"
  light-on-primary: "#FFFFFF"
  light-primary-container: "#B1F6C6"
  light-on-primary-container: "#00210D"
  light-background: "#F3FAF4"
  light-on-background: "#0F2116"
  light-surface: "#F7FFF8"
  light-on-surface: "#0F2116"
  light-surface-variant: "#DFEDE1"
  light-on-surface-variant: "#405A48"
  light-secondary-container: "#D7EADB"
  light-on-secondary-container: "#102C1B"
  light-outline: "#647A69"
  light-outline-variant: "#B7CCBD"
  light-secondary: "#3C674C"
  light-on-secondary: "#FFFFFF"
  light-surface-tint: "#007B36"
  light-surface-container-lowest: "#FFFFFF"
  light-surface-container-low: "#F0F8F0"
  light-surface-container: "#EAF3EB"
  light-surface-container-high: "#E4EEE5"
  light-surface-container-highest: "#DEEADE"
typography:
  display-large:
    fontFamily: "Roboto, system Sans"
    fontSize: "48sp"
    fontWeight: 700
    lineHeight: "52sp"
  display-small:
    fontFamily: "Roboto, system Sans"
    fontSize: "38sp"
    fontWeight: 700
    lineHeight: "43sp"
  headline-large:
    fontFamily: "Roboto, system Sans"
    fontSize: "32sp"
    fontWeight: 700
    lineHeight: "38sp"
  headline-medium:
    fontFamily: "Roboto, system Sans"
    fontSize: "27sp"
    fontWeight: 700
    lineHeight: "33sp"
  headline-small:
    fontFamily: "Roboto, system Sans"
    fontSize: "24sp"
    fontWeight: 700
    lineHeight: "30sp"
  title-large:
    fontFamily: "Roboto, system Sans"
    fontSize: "21sp"
    fontWeight: 700
    lineHeight: "27sp"
  title-medium:
    fontFamily: "Roboto, system Sans"
    fontSize: "16sp"
    fontWeight: 600
    lineHeight: "22sp"
  title-small:
    fontFamily: "Roboto, system Sans"
    fontSize: "14sp"
    fontWeight: 600
    lineHeight: "20sp"
  body-large:
    fontFamily: "Roboto, system Sans"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: "24sp"
  body-medium:
    fontFamily: "Roboto, system Sans"
    fontSize: "14sp"
    fontWeight: 400
    lineHeight: "21sp"
  body-small:
    fontFamily: "Roboto, system Sans"
    fontSize: "12sp"
    fontWeight: 400
    lineHeight: "18sp"
  label-large:
    fontFamily: "Roboto, system Sans"
    fontSize: "14sp"
    fontWeight: 700
    lineHeight: "20sp"
  label-medium:
    fontFamily: "Roboto, system Sans"
    fontSize: "12sp"
    fontWeight: 500
    lineHeight: "16sp"
rounded:
  compact-row: "12dp"
  artwork: "14dp"
  track-row: "16dp"
spacing:
  inline-tight: "4dp"
  inline: "8dp"
  compact-gap: "10dp"
  control-gap: "12dp"
  art-caption: "14dp"
  art-gap: "16dp"
  section: "18dp"
  screen: "20dp"
  form-screen: "24dp"
  player-screen: "26dp"
  welcome-screen: "28dp"
components:
  button-primary:
    backgroundColor: "{colors.dark-primary}"
    textColor: "{colors.dark-on-primary}"
    typography: "{typography.label-large}"
    height: "54dp"
  button-text:
    textColor: "{colors.dark-primary}"
    typography: "{typography.label-large}"
  button-play:
    backgroundColor: "{colors.sortify-green}"
    textColor: "{colors.dark-on-primary}"
    size: "60dp"
  button-play-full:
    backgroundColor: "{colors.sortify-green}"
    textColor: "{colors.dark-on-primary}"
    size: "74dp"
  filter-selected:
    backgroundColor: "{colors.dark-primary}"
    textColor: "{colors.dark-on-primary}"
    height: "48dp"
  filter-unselected:
    textColor: "{colors.dark-on-surface-variant}"
    height: "48dp"
  field-outlined:
    textColor: "{colors.dark-on-surface}"
    rounded: "{rounded.artwork}"
  artwork-card:
    rounded: "{rounded.artwork}"
    width: "148dp"
  song-row:
    backgroundColor: "{colors.dark-surface}"
    textColor: "{colors.dark-on-surface}"
    rounded: "{rounded.track-row}"
    padding: "9dp 2dp 9dp 10dp"
  song-row-active:
    backgroundColor: "{colors.sortify-green}"
    textColor: "{colors.dark-on-primary}"
    rounded: "{rounded.track-row}"
    padding: "9dp 2dp 9dp 10dp"
  mini-player:
    backgroundColor: "{colors.dark-secondary-container}"
    textColor: "{colors.dark-on-surface}"
    rounded: "{rounded.track-row}"
  navigation-compact:
    textColor: "{colors.dark-on-background}"
    typography: "{typography.label-medium}"
    height: "72dp"
---

# Design System: Sortify

## Overview

**Creative North Star: "The supplied forest-green music reference"**

Sortify uses the user's pinned forest-green music reference and supplied green S mark. Near-black green surfaces, clear white type, bright playback controls, square artwork, and circular artists carry that reference across a native Kotlin and Jetpack Compose app. This is an Operate surface: discovery stays image-led while controls and labels remain familiar Android controls.

The light appearance preserves the same role relationships through pale green surfaces, dark text, and a deeper green primary. The interface is compact enough for browsing a music collection, with larger welcome and section headings creating hierarchy. The product needs no account; the visual system supports a local personal library without implying a connected sign-in service.

**Key Characteristics:**

- Forest-green tonal surfaces with vivid green playback and active-track states.
- Artwork-led browsing with captions outside artwork clipping.
- Roboto/system Sans with bold headings and quiet supporting text.
- Persistent playback controls and native phone/tablet navigation.
- Dark, Light, and System appearances with the same semantic roles.

This scan records the implemented native system; it does not establish a new visual direction. Product constraints are in [PRODUCT.md](PRODUCT.md), and the surface direction is in [.impeccable/surfaces/android-app.md](.impeccable/surfaces/android-app.md).

Trace: [pinned showcase](docs/references/design-showcase.png), [reference and logo provenance](docs/references/PROVENANCE.md), [shipping artwork provenance](app/src/main/assets/provenance.json). Actual role colors and type styles come from [Theme.kt](app/src/main/java/com/sortify/app/ui/Theme.kt); shared forms come from [Components.kt](app/src/main/java/com/sortify/app/ui/Components.kt).

Reviewed captures: [phone home](docs/screenshots/home-phone.png), [tablet home](docs/screenshots/home-tablet.png), [player](docs/screenshots/player-phone.png), [quality dialog](docs/screenshots/quality-phone.png), [light settings](docs/screenshots/settings-light-phone.png). The finish review's caption-clipping issue was fixed and resolved in the phone, tablet, and increased-font recaptures ([font-scale home](docs/screenshots/home-font130-phone.png)); the reviewer disposition for that fix was ship, with no remaining findings. No web detector applies to this native surface.

## Colors

The palette uses one green identity with green-tinted neutrals. The frontmatter records the explicit theme assignments under `dark-*` and `light-*`; it does not fabricate roles inherited from Material 3 defaults.

### Primary

- **Sortify green:** The fixed `sortify-green` color identifies circular play/pause controls and the current track row in both themes.
- **Dark primary:** The vivid green theme primary identifies the brand, selected filters, selected preferences, favourites, and progress.
- **Light primary:** A deeper green preserves the primary role on pale surfaces.
- **Primary containers and paired foregrounds:** Toned green areas distinguish selection, onboarding language tiles, empty-state icons, and the active navigation indicator.

**The Role Pairing Rule.** Use each theme's foreground with its matching container. Playback green and active-track green keep the deep-green foreground in both appearances.

### Secondary

- **Secondary and secondary container:** Muted green supporting roles; the secondary container is the persistent mini-player's surface. These are supporting tones within the green identity, rather than a second accent hue.
- **On-secondary and on-secondary-container:** Paired content roles for those native Material surfaces.

### Neutral

- **Background / on-background:** The forest canvas and clear main type in dark appearance; pale green canvas and deep green type in light appearance.
- **Surface / on-surface:** Quiet track rows, sheets, and their principal content.
- **Surface variant / on-surface-variant:** Artwork fallbacks, retry notices, metadata, descriptions, and secondary icons.
- **Surface container range:** Lowest through highest tonal layers support native dialogs and containers without introducing a custom shadow scale.
- **Outline / outline variant:** Outlined controls, language tiles, and the restrained mini-player boundary.
- **Inverse roles:** Explicit dark-theme inverse assignments are retained for native Material feedback.
- **Error / on-error:** Explicit dark-theme error roles are retained. The current shared retry notice uses the neutral surface-variant treatment with an explicit Retry action.

Black, white, and transparent artwork scrims are compositing treatments in the implementation, not extra brand accents. Appearance switches between Dark, Light, and System; the UI does not derive its palette from the device wallpaper.

## Typography

**Display Font:** Roboto / Android system Sans.  
**Body Font:** Roboto / Android system Sans.  
**Label Font:** The same native sans family.

**Character:** Bold, compact headings lead; medium and regular supporting text keeps dense lists readable. This Operate surface uses the native sans family rather than introducing an expressive display face. Only displayLarge explicitly sets Compose SansSerif; the remaining styles inherit the native Material sans family.

### Hierarchy

The frontmatter contains the complete explicit scale, including line heights and the negative tracking used on the largest headings. Sizes are Android sp, not CSS pixels; there is no single imposed scale ratio.

| Source style | Use |
| --- | --- |
| displayLarge / displaySmall | Welcome statement and available large display role |
| headlineLarge | Home, search, collection, creation, and settings introductions |
| headlineMedium / headlineSmall | Player title, queue introduction, empty-state title, and sheet heading |
| titleLarge | Section titles, back headers, language labels, and browse titles |
| titleMedium / titleSmall | Track, playlist, result, mini-player, and artist-related labels |
| bodyLarge / bodyMedium / bodySmall | Welcome explanation, artist names, descriptions, metadata, and helper copy |
| labelLarge / labelMedium | Actions, filter labels, compact navigation, and artist captions |

Track rows and song-carousel titles deliberately ellipsize to one line; the main player title and artist can use two lines, and playlist headings can use three. Supporting captions keep their own measured height.

**The Caption Boundary Rule.** Clip artwork and collages to their image shape; never apply the artwork clip to the Column that also contains captions.

## Layout

Dimensions use dp and text uses sp. Most collection and discovery screens use horizontal screen padding (20dp); forms commonly use 24dp, the full player 26dp, and welcome 28dp. Repeated gaps cluster around 8, 10, 12, 14, 16, and 18dp. These are observed steps, not a new grid constraint.

Discovery uses vertical scrolling with horizontal artwork shelves. Standard shelf art is 148dp square with 16dp gaps; artist portraits are 90dp circles with their own captions. Search moods use a two-column arrangement. The main content respects safe drawing insets and keyboard insets.

Below 700dp available width, Home, Search, Your library, and Create live in a bottom navigation bar with a minimum height of 72dp. At 700dp and above they move to a native leading rail; expanded main content is centered and capped at 900dp (the compact cap is 700dp). This is an adaptive native layout, not a stretched phone canvas.

The mini-player remains in the scaffold bottom area above compact navigation whenever a current track exists; on expanded layouts it remains below main content beside the rail. Full Player and Queue cover this navigation and mini-player with an overlay, keeping the browsing composition and its scroll position mounted underneath. Covered browsing content is hidden from accessibility while either route is open. Scroll content receives scaffold padding so it can be reached above those controls.

The player cover size is the smallest of available width minus 56dp, 39% of available height, and 390dp. The player itself scrolls on short screens. Welcome switches to a shorter mark composition below 650dp available height.

## Elevation & Depth

The base system is flat and tonal: a subtle green vertical background wash, surface-role changes, fine outlines, and dark artwork scrims establish hierarchy. The mini-player has a fine boundary rather than a custom drop shadow. Modals and sheets use native Material 3 elevation and scrims; there is no app-defined shadow scale. The player overlay follows vertical drags and settles with FastOutSlowIn easing: 320ms to expand and 240ms to collapse, respecting Android animation duration scale.

**The Tonal Depth Rule.** Use the existing green surface roles, restrained borders, and artwork scrims for hierarchy. Leave modal depth and interaction feedback to native Material 3.

Do not reinterpret the background wash as a blur, glass panel, or decorative card stack. Native Material controls retain their platform interaction feedback; the equalizer is a static playing indicator, not a bespoke animation system.

## Shapes

Artwork and collages share softly rounded squares (14dp). Track rows, mood tiles, language tiles, and the mini-player use the broader row corner (16dp). Compact playlist/result hit areas and retry notices use 12dp corners. Artists, play controls, empty-state icon disks, and filter pills use Compose CircleShape. Some collection hero icons use a larger local corner treatment; it is not a replacement for the shared artwork shape.

The image alone owns its crop and clip. Artwork uses a crop fill and a music-note fallback on the surface-variant background. Carousel and artist Columns that contain both an image and text must remain unclipped, including when titles wrap or text scale increases.

## Components

### Buttons

Circular green play controls are the strongest action: 60dp by default and 74dp in the full player. Their icon changes between Play and Pause, with a spinner during buffering. The fixed green and deep-green foreground stay paired in both themes.

Native filled buttons carry the theme primary. Onboarding and form submit actions span their content width and use observed minimum heights of 54–56dp. Blank playlist names and empty language selections disable submission. Text buttons carry secondary actions such as See all, Retry, and dialog dismissal.

Shared icon actions reserve at least 48dp in each axis and have content descriptions. Press, focus, disabled, and radio/checkbox feedback comes from Material 3; no custom web hover treatment defines the native app.

### Chips

Filter chips are outlined pills with at least 48dp height. Selection fills the pill with theme primary and its paired on-primary text. They select discovery, library, or search categories and retain readable native selected/unselected states.

### Cards / Containers

Artwork leads rather than sitting inside an additional decorative card. A 148dp shelf item consists of clipped artwork and a separate caption region. Mix artwork may contain a bottom scrim and title; its description remains outside the art. Circular artist portraits follow the same caption boundary rule.

Language tiles use a primary-container fill, primary border, and check indicator when selected; unselected tiles use the quiet surface and outline-variant border. Library shortcuts and playlist rows combine a small cover or icon with a text column and chevron.

### Inputs / Fields

Search and the main creation field are native outlined fields with the shared artwork corner (14dp), a single-line input, and the theme's native focus treatment. Search includes a leading search icon, a clear action when populated, and a keyboard Search action. Playlist creation shows a character count with an 80-character limit. Smaller sheet and rename fields retain their native Material shape defaults.

### Navigation

Compact navigation keeps four icon-and-label destinations; the selected item uses on-background content over a subtle primary-container indicator. Wider layouts replace it with a leading rail and Sortify mark. Back headers use a native back action and title. Player dismissal, queue entry, and bottom-sheet actions preserve these native interaction patterns.

### Track row

A 54dp artwork thumbnail, weighted title/artist column, and options action sit in a 16dp rounded row. The active track fills with fixed Sortify green and deep-green content. Playing adds the small equalizer bars; downloaded tracks include a check indicator. Rows distinguish active selection from playing so a paused current track remains identifiable.

### Persistent mini-player

A secondary-container surface with a fine outline and 16dp corners holds 48dp art, title, artist, favourite, and play/pause actions. A 2dp progress strip anchors its bottom. Tapping or swiping up on the surface opens the full player; swiping down on the full player header or artwork collapses it. Short, slow drags return to the initial state, directional flings take precedence, and horizontal drags cancel accidental taps. The seek bar and transport controls remain outside the dismissal gesture area; buffering replaces play/pause with a spinner, and liked state changes the heart and its tint.

### Dialogs, sheets, and feedback

Quality, appearance, and sleep-timer choices use native radio dialogs. Quality offers 96, 160, and 320kbps with a data/storage explanation. Destructive download and playlist actions use explicit confirmation dialogs. Song actions and add-to-playlist use modal bottom sheets.

Loading uses native linear or circular progress indicators. Shared retry notices pair explanation and Retry. Empty library, downloads, liked songs, and playlist states use an icon disk, a clear title, supporting text, and an appropriate next action. Search separates browse, loading, results, no matches, and service-error states. Snackbars acknowledge actions. These states must remain part of a screen's design rather than assuming only populated success.

Sources: [MainActivity.kt](app/src/main/java/com/sortify/app/MainActivity.kt), [DiscoveryScreens.kt](app/src/main/java/com/sortify/app/ui/DiscoveryScreens.kt), [LibraryScreens.kt](app/src/main/java/com/sortify/app/ui/LibraryScreens.kt), [PlayerScreens.kt](app/src/main/java/com/sortify/app/ui/PlayerScreens.kt), and [SettingsScreen.kt](app/src/main/java/com/sortify/app/ui/SettingsScreen.kt).

The companion [.impeccable/design.json](.impeccable/design.json) extends this document with metadata and self-contained browser specimens. Those snippets translate native components for the design panel only: their CSS px stand in for dp/sp at a preview scale of one, and their browser hover/focus rules are panel affordances. Compose source and this document's Android units remain authoritative. Synthesized sidecar tonal strips are palette previews, not additional app tokens.

## Do's and Don'ts

### Do:

- **Do** use the supplied Sortify S mark and the theme's green primary for the brand.
- **Do** use dp for dimensions and sp for text, with Roboto/system Sans and the extracted type roles.
- **Do** preserve at least 48dp targets for shared icon actions and filter chips.
- **Do** keep the mini-player above compact navigation whenever a current track exists.
- **Do** let captions and supporting text measure to their content height, including at increased font scale.
- **Do** show loading, retry, no-results, empty-library, selected, playing, paused, and downloaded states using the shared patterns.
- **Do** use the rail at 700dp and above, with the main content constrained to 900dp.

### Don't:

- **Don't** replace the supplied forest-green visual direction or green Sortify identity with a new palette or mark.
- **Don't** clip caption Columns to artwork corners or a fixed artwork height.
- **Don't** use the bright playback green with white text; preserve its deep-green foreground.
- **Don't** stretch the player cover beyond its responsive size cap or place compact navigation on the full player and queue screens.
- **Don't** add a sign-in presentation that implies an account service exists.

