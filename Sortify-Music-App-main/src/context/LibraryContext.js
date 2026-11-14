import React, { createContext, useContext, useEffect, useState, useCallback } from "react";
import AsyncStorage from "@react-native-async-storage/async-storage";

const STORAGE_KEY = "sortify:library:v1";
const LibraryContext = createContext(null);

const initialState = {
  recentlyPlayed: [], // normalized song objects, most recent first, capped at 12
  favorites: [], // normalized song objects
  playlists: [], // { id, name, songs: [] }
  downloads: [], // normalized song objects with localUri
};

export function LibraryProvider({ children }) {
  const [state, setState] = useState(initialState);
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const raw = await AsyncStorage.getItem(STORAGE_KEY);
        if (raw) setState({ ...initialState, ...JSON.parse(raw) });
      } catch (e) {
        // ignore corrupt storage, start fresh
      } finally {
        setLoaded(true);
      }
    })();
  }, []);

  useEffect(() => {
    if (!loaded) return;
    AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(state)).catch(() => {});
  }, [state, loaded]);

  const addRecentlyPlayed = useCallback((song) => {
    setState((prev) => {
      const withoutDupe = prev.recentlyPlayed.filter((s) => s.id !== song.id);
      return { ...prev, recentlyPlayed: [song, ...withoutDupe].slice(0, 12) };
    });
  }, []);

  const isFavorite = useCallback(
    (songId) => state.favorites.some((s) => s.id === songId),
    [state.favorites]
  );

  const toggleFavorite = useCallback((song) => {
    setState((prev) => {
      const exists = prev.favorites.some((s) => s.id === song.id);
      return {
        ...prev,
        favorites: exists
          ? prev.favorites.filter((s) => s.id !== song.id)
          : [song, ...prev.favorites],
      };
    });
  }, []);

  const createPlaylist = useCallback((name) => {
    const id = `pl_${Date.now()}`;
    setState((prev) => ({
      ...prev,
      playlists: [...prev.playlists, { id, name, songs: [] }],
    }));
    return id;
  }, []);

  const deletePlaylist = useCallback((playlistId) => {
    setState((prev) => ({
      ...prev,
      playlists: prev.playlists.filter((p) => p.id !== playlistId),
    }));
  }, []);

  const addSongToPlaylist = useCallback((playlistId, song) => {
    setState((prev) => ({
      ...prev,
      playlists: prev.playlists.map((p) =>
        p.id === playlistId && !p.songs.some((s) => s.id === song.id)
          ? { ...p, songs: [...p.songs, song] }
          : p
      ),
    }));
  }, []);

  const removeSongFromPlaylist = useCallback((playlistId, songId) => {
    setState((prev) => ({
      ...prev,
      playlists: prev.playlists.map((p) =>
        p.id === playlistId
          ? { ...p, songs: p.songs.filter((s) => s.id !== songId) }
          : p
      ),
    }));
  }, []);

  const isDownloaded = useCallback(
    (songId) => state.downloads.some((s) => s.id === songId),
    [state.downloads]
  );

  const addDownload = useCallback((song) => {
    setState((prev) =>
      prev.downloads.some((s) => s.id === song.id)
        ? prev
        : { ...prev, downloads: [song, ...prev.downloads] }
    );
  }, []);

  const removeDownload = useCallback((songId) => {
    setState((prev) => ({
      ...prev,
      downloads: prev.downloads.filter((s) => s.id !== songId),
    }));
  }, []);

  return (
    <LibraryContext.Provider
      value={{
        ...state,
        loaded,
        addRecentlyPlayed,
        isFavorite,
        toggleFavorite,
        createPlaylist,
        deletePlaylist,
        addSongToPlaylist,
        removeSongFromPlaylist,
        isDownloaded,
        addDownload,
        removeDownload,
      }}
    >
      {children}
    </LibraryContext.Provider>
  );
}

export function useLibrary() {
  const ctx = useContext(LibraryContext);
  if (!ctx) throw new Error("useLibrary must be used within LibraryProvider");
  return ctx;
}
