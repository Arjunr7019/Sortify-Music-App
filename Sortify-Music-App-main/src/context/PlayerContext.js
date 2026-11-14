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

  const lockScreenActiveRef = useRef(false);
  const lockScreenMetadataRef = useRef(null);
  const durationPushedRef = useRef(false);

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

      durationPushedRef.current = false;
      player.replace(url);
      player.play();

      // Lock-screen/notification metadata is best-effort: a failure here
      // (e.g. the native service isn't built into this binary yet) should
      // never stop in-app playback.
      const metadata = {
        title: song.name,
        artist: song.artist,
        albumTitle: song.album || undefined,
        artworkUrl: song.image || undefined,
      };
      lockScreenMetadataRef.current = metadata;
      try {
        if (!lockScreenActiveRef.current) {
          await player.setActiveForLockScreen(true, metadata, {
            showSeekBackward: true,
            showSeekForward: true,
          });
          lockScreenActiveRef.current = true;
        } else {
          await player.updateLockScreenMetadata(metadata);
        }
      } catch (e) {
        // Most commonly means the app needs a fresh native build after
        // the expo-audio config plugin was added/changed — see README.
        lockScreenActiveRef.current = false;
      }

      setCurrent(song);
      addRecentlyPlayed(song);
    },
    [player, addRecentlyPlayed]
  );

  // A freshly-loaded remote stream reports duration as 0/unknown for a
  // moment while it buffers, so the very first setActiveForLockScreen call
  // above often captures a duration of 0 — which is why the lock-screen
  // progress bar can get stuck at zero even though in-app playback (which
  // reads live status) looks fine. Once the real duration comes through,
  // push the metadata again so the OS picks it up.
  useEffect(() => {
    if (
      lockScreenActiveRef.current &&
      !durationPushedRef.current &&
      status.duration > 0 &&
      lockScreenMetadataRef.current
    ) {
      durationPushedRef.current = true;
      (async () => {
        try {
          await player.updateLockScreenMetadata(lockScreenMetadataRef.current);
        } catch (e) {
          // Service not connected (e.g. running in Expo Go instead of a
          // dev client) — safe to ignore, in-app playback is unaffected.
        }
      })();
    }
  }, [status.duration, player]);

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
