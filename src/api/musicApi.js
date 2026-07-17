// Thin client around the Saavn wrapper API. Every endpoint here is a GET
// request, matching the reference doc supplied with this project.
const BASE_URL = "https://saavn.sumit.co/api";

async function getJSON(url) {
  const res = await fetch(url);
  if (!res.ok) {
    throw new Error(`Request failed (${res.status}): ${url}`);
  }
  const json = await res.json();
  if (json && json.success === false) {
    throw new Error(json.message || "API returned an error");
  }
  return json.data;
}

// ---- Search -----------------------------------------------------------

// Global search across songs, albums, artists, playlists, top query.
export function globalSearch(query) {
  return getJSON(`${BASE_URL}/search?query=${encodeURIComponent(query)}`);
}

export function searchSongs(query, page = 0, limit = 20) {
  return getJSON(
    `${BASE_URL}/search/songs?query=${encodeURIComponent(query)}&page=${page}&limit=${limit}`
  );
}

export function searchAlbums(query) {
  return getJSON(`${BASE_URL}/search/albums?query=${encodeURIComponent(query)}`);
}

export function searchArtists(query) {
  return getJSON(`${BASE_URL}/search/artists?query=${encodeURIComponent(query)}`);
}

export function searchPlaylists(query) {
  return getJSON(`${BASE_URL}/search/playlists?query=${encodeURIComponent(query)}`);
}

// ---- Songs --------------------------------------------------------------

export function getSongById(id) {
  return getJSON(`${BASE_URL}/songs/${id}`);
}

export function getSongSuggestions(id) {
  return getJSON(`${BASE_URL}/songs/${id}/suggestions`);
}

// ---- Artists --------------------------------------------------------------

export function getArtistById(id) {
  return getJSON(`${BASE_URL}/artists/${id}`);
}

export function getArtistSongs(id) {
  return getJSON(`${BASE_URL}/artists/${id}/songs`);
}

export function getArtistAlbums(id) {
  return getJSON(`${BASE_URL}/artists/${id}/albums`);
}

// ---- Derived helpers ------------------------------------------------------
// The API has no dedicated "trending" or "by language" endpoint, so trending
// rows are built from language-targeted searches, and playlist rows from the
// playlist search endpoint.

export async function getTrendingForLanguage(language, limit = 15) {
  const data = await searchSongs(`${language} hit songs`, 0, limit);
  return data?.results ?? [];
}

export async function getPlaylistsForLanguage(language) {
  const data = await searchPlaylists(`${language} top playlist`);
  return data?.results ?? [];
}

// Normalizes a "search/songs" style result and a "songs/:id" style result
// into one flat shape the UI can rely on everywhere.
export function normalizeSong(song) {
  if (!song) return null;
  const images = song.image || [];
  const bestImage =
    images[images.length - 1]?.url || images[0]?.url || null;
  const downloadUrls = song.downloadUrl || [];
  const bestAudio =
    downloadUrls[downloadUrls.length - 1]?.url ||
    downloadUrls[0]?.url ||
    null;
  const primaryArtists =
    song.artists?.primary?.map((a) => a.name).join(", ") ||
    song.primaryArtists ||
    song.singers ||
    "Unknown Artist";

  return {
    id: song.id,
    name: song.name || song.title,
    artist: primaryArtists,
    album: song.album?.name || song.album || "",
    image: bestImage,
    audioUrl: bestAudio,
    // Full list of { quality, url } options straight from the API, so
    // playback can pick a specific bitrate at play time (see
    // resolveAudioUrl below) rather than always using the highest one.
    downloadUrls,
    duration: song.duration || null,
    language: song.language || "",
  };
}

// Streaming quality options actually offered by the API's downloadUrl
// entries (e.g. "96kbps", "160kbps", "320kbps").
export const QUALITY_OPTIONS = ["96kbps", "160kbps", "320kbps"];
export const DEFAULT_QUALITY = "160kbps";

// Resolve the best URL for a song at a given quality, falling back to the
// closest available bitrate (then the song's default) if that exact
// quality wasn't returned for this track.
export function resolveAudioUrl(song, quality) {
  const list = song?.downloadUrls;
  if (!list || list.length === 0) return song?.audioUrl || null;

  const exact = list.find((d) => d.quality === quality);
  if (exact?.url) return exact.url;

  const rank = (q) => parseInt(String(q).replace(/[^0-9]/g, ""), 10) || 0;
  const target = rank(quality);
  const sorted = [...list].sort((a, b) => Math.abs(rank(a.quality) - target) - Math.abs(rank(b.quality) - target));
  return sorted[0]?.url || song?.audioUrl || null;
}
