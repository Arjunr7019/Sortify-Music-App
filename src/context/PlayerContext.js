import React, { createContext, useContext, useEffect, useRef, useState, useCallback } from "react";
import { Audio } from "expo-av";
import { useLibrary } from "./LibraryContext";

const PlayerContext = createContext(null);

export function PlayerProvider({ children }) {
  const soundRef = useRef(null);
  const [queue, setQueue] = useState([]);
  const [queueIndex, setQueueIndex] = useState(0);
  const [current, setCurrent] = useState(null); // normalized song
  const [isPlaying, setIsPlaying] = useState(false);
  const [isBuffering, setIsBuffering] = useState(false);
  const [positionMillis, setPositionMillis] = useState(0);
  const [durationMillis, setDurationMillis] = useState(0);
  const [sheetExpanded, setSheetExpanded] = useState(false);

  const { addRecentlyPlayed } = useLibrary();

  useEffect(() => {
    Audio.setAudioModeAsync({
      playsInSilentModeIOS: true,
      staysActiveInBackground: true,
      shouldDuckAndroid: true,
    }).catch(() => {});
    return () => {
      if (soundRef.current) soundRef.current.unloadAsync();
    };
  }, []);

  const onStatusUpdate = useCallback(
    (status) => {
      if (!status.isLoaded) {
        setIsBuffering(true);
        return;
      }
      setIsBuffering(status.isBuffering);
      setIsPlaying(status.isPlaying);
      setPositionMillis(status.positionMillis || 0);
      setDurationMillis(status.durationMillis || 0);
      if (status.didJustFinish) {
        playNext();
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [queue, queueIndex]
  );

  const loadAndPlay = useCallback(
    async (song) => {
      if (!song?.audioUrl) return;
      setCurrent(song);
      setIsBuffering(true);
      try {
        if (soundRef.current) {
          await soundRef.current.unloadAsync();
          soundRef.current = null;
        }
        const { sound } = await Audio.Sound.createAsync(
          { uri: song.audioUrl },
          { shouldPlay: true },
          onStatusUpdate
        );
        soundRef.current = sound;
        addRecentlyPlayed(song);
      } catch (e) {
        setIsBuffering(false);
      }
    },
    [onStatusUpdate, addRecentlyPlayed]
  );

  // Start playing a song, optionally with a queue (e.g. a whole trending row)
  const playSong = useCallback(
    (song, songQueue) => {
      const q = songQueue && songQueue.length ? songQueue : [song];
      const idx = q.findIndex((s) => s.id === song.id);
      setQueue(q);
      setQueueIndex(idx === -1 ? 0 : idx);
      loadAndPlay(song);
      setSheetExpanded(true);
    },
    [loadAndPlay]
  );

  const togglePlayPause = useCallback(async () => {
    if (!soundRef.current) return;
    const status = await soundRef.current.getStatusAsync();
    if (status.isPlaying) {
      await soundRef.current.pauseAsync();
    } else {
      await soundRef.current.playAsync();
    }
  }, []);

  const playNext = useCallback(() => {
    setQueue((q) => {
      setQueueIndex((idx) => {
        const nextIdx = idx + 1 < q.length ? idx + 1 : 0;
        if (q[nextIdx]) loadAndPlay(q[nextIdx]);
        return nextIdx;
      });
      return q;
    });
  }, [loadAndPlay]);

  const playPrevious = useCallback(() => {
    setQueue((q) => {
      setQueueIndex((idx) => {
        const prevIdx = idx - 1 >= 0 ? idx - 1 : q.length - 1;
        if (q[prevIdx]) loadAndPlay(q[prevIdx]);
        return prevIdx;
      });
      return q;
    });
  }, [loadAndPlay]);

  const seekTo = useCallback(async (millis) => {
    if (!soundRef.current) return;
    await soundRef.current.setPositionAsync(millis);
  }, []);

  return (
    <PlayerContext.Provider
      value={{
        current,
        isPlaying,
        isBuffering,
        positionMillis,
        durationMillis,
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
