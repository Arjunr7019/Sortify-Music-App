import React, { createContext, useContext, useEffect, useRef, useState, useCallback } from "react";
import { Platform } from "react-native";
import {
  createAudioPlayer,
  useAudioPlayerStatus,
  setAudioModeAsync,
  requestNotificationPermissionsAsync,
} from "expo-audio";
import { useLibrary } from "./LibraryContext";
import { useQuality } from "./QualityContext";
import { resolveAudioUrl } from "../api/musicApi";

const PlayerContext = createContext(null);

export function PlayerProvider({ children }) {
  const [player] = useState(() => createAudioPlayer(null));
  const [current, setCurrent] = useState(null); // normalized song
  const [sheetExpanded, setSheetExpanded] = useState(false);

  const { addRecentlyPlayed } = useLibrary();
  const { quality } = useQuality();

  const status = useAudioPlayerStatus(player);

  // App-level queue — expo-audio's AudioPlayer is a single track, so we
  // manage the "which song is next/previous" ourselves and call
  // player.replace() to move between them.
  const queueRef = useRef([]);
  const queueIndexRef = useRef(0);
  const qualityRef = useRef(quality);
  useEffect(() => {
    qualityRef.current = quality;
  }, [quality]);

  // One-time audio session setup so playback survives backgrounding and
  // the OS treats it as "now playing" for lock-screen controls.
  useEffect(() => {
    setAudioModeAsync({
      playsInSilentMode: true,
      shouldPlayInBackground: true,
      interruptionMode: "doNotMix",
    }).catch(() => {});
    if (Platform.OS === "android") {
      requestNotificationPermissionsAsync().catch(() => {});
    }
    return () => {
      player.remove();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadIndex = useCallback(
    async (idx) => {
      const q = queueRef.current;
      if (!q.length) return;
      const wrapped = ((idx % q.length) + q.length) % q.length;
      const song = q[wrapped];
      queueIndexRef.current = wrapped;

      const isLocalFile = typeof song.audioUrl === "string" && song.audioUrl.startsWith("file://");
      const url = isLocalFile ? song.audioUrl : resolveAudioUrl(song, qualityRef.current);
      if (!url) return;

      player.replace(url);
      player.setActiveForLockScreen(
        true,
        {
          title: song.name,
          artist: song.artist,
          albumTitle: song.album || undefined,
          artworkUrl: song.image || undefined,
        },
        { showSeekBackward: true, showSeekForward: true }
      );
      player.play();

      setCurrent(song);
      addRecentlyPlayed(song);
    },
    [player, addRecentlyPlayed]
  );

  const playSong = useCallback(
    async (song, songQueue) => {
      if (!song?.id) return;
      const q = songQueue && songQueue.length ? songQueue : [song];
      queueRef.current = q;
      const idx = q.findIndex((s) => s.id === song.id);
      await loadIndex(idx === -1 ? 0 : idx);
      setSheetExpanded(true);
    },
    [loadIndex]
  );

  const togglePlayPause = useCallback(() => {
    if (status.playing) {
      player.pause();
    } else {
      player.play();
    }
  }, [player, status.playing]);

  const playNext = useCallback(() => {
    loadIndex(queueIndexRef.current + 1);
  }, [loadIndex]);

  const playPrevious = useCallback(() => {
    loadIndex(queueIndexRef.current - 1);
  }, [loadIndex]);

  const seekTo = useCallback(
    (millis) => {
      player.seekTo(millis / 1000);
    },
    [player]
  );

  // Auto-advance when the current track finishes.
  const didJustFinishRef = useRef(false);
  useEffect(() => {
    if (status.didJustFinish && !didJustFinishRef.current) {
      didJustFinishRef.current = true;
      playNext();
    } else if (!status.didJustFinish) {
      didJustFinishRef.current = false;
    }
  }, [status.didJustFinish, playNext]);

  return (
    <PlayerContext.Provider
      value={{
        current,
        isPlaying: status.playing,
        isBuffering: status.isBuffering,
        positionMillis: (status.currentTime || 0) * 1000,
        durationMillis: (status.duration || 0) * 1000,
        sheetExpanded,
        setSheetExpanded,
        playSong,
        togglePlayPause,
        playNext,
        playPrevious,
        seekTo,
        hasTrack: !!current,
      }}
    >
      {children}
    </PlayerContext.Provider>
  );
}

export function usePlayer() {
  const ctx = useContext(PlayerContext);
  if (!ctx) throw new Error("usePlayer must be used within PlayerProvider");
  return ctx;
}
