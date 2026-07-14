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
    duration: song.duration || null,
    language: song.language || "",
  };
}
