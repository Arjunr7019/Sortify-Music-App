import React, { useRef, useState, useEffect, useMemo } from "react";
import {
  Animated,
  PanResponder,
  View,
  Text,
  Image,
  TouchableOpacity,
  StyleSheet,
  Dimensions,
  ActivityIndicator,
  Alert,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import Slider from "@react-native-community/slider";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useAppTheme } from "../context/ThemeContext";
import { usePlayer } from "../context/PlayerContext";
import { useLibrary } from "../context/LibraryContext";
import { formatMillis } from "../utils/format";
import { downloadSongFile } from "../utils/download";
import AddToPlaylistModal from "./AddToPlaylistModal";

const { height: SCREEN_H, width: SCREEN_W } = Dimensions.get("window");
const MINI_HEIGHT = 66;
const TAB_BAR_HEIGHT = 70;

export default function PlayerSheet() {
  const insets = useSafeAreaInsets();
  const { theme } = useAppTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);
  const {
    current,
    isPlaying,
    isBuffering,
    positionMillis,
    durationMillis,
    sheetExpanded,
    setSheetExpanded,
    togglePlayPause,
    playNext,
    playPrevious,
    seekTo,
    hasTrack,
  } = usePlayer();
  const { isFavorite, toggleFavorite, isDownloaded, addDownload } = useLibrary();

  const progress = useRef(new Animated.Value(0)).current;
  const [dragging, setDragging] = useState(false);
  const [sliderValue, setSliderValue] = useState(0);
  const [downloading, setDownloading] = useState(false);
  const [showAddToPlaylist, setShowAddToPlaylist] = useState(false);

  useEffect(() => {
    Animated.spring(progress, {
      toValue: sheetExpanded ? 1 : 0,
      useNativeDriver: false,
      bounciness: 4,
    }).start();
  }, [sheetExpanded]);

  const panResponder = useRef(
    PanResponder.create({
      onMoveShouldSetPanResponder: (_, g) =>
        Math.abs(g.dy) > 12 && Math.abs(g.dy) > Math.abs(g.dx) * 1.5,
      onPanResponderMove: (_, g) => {
        const base = sheetExpanded ? 1 : 0;
        const delta = -g.dy / (SCREEN_H - MINI_HEIGHT);
        let val = base + delta;
        val = Math.max(0, Math.min(1, val));
        progress.setValue(val);
      },
      onPanResponderRelease: (_, g) => {
        const shouldExpand = g.dy < -40 || (sheetExpanded && g.dy < 60);
        const shouldCollapse = g.dy > 40;
        if (sheetExpanded) {
          setSheetExpanded(!shouldCollapse);
        } else {
          setSheetExpanded(shouldExpand);
        }
      },
    })
  ).current;

  if (!hasTrack || !current) return null;

  const bottom = progress.interpolate({
    inputRange: [0, 1],
    outputRange: [TAB_BAR_HEIGHT + insets.bottom, 0],
  });
  const height = progress.interpolate({
    inputRange: [0, 1],
    outputRange: [MINI_HEIGHT, SCREEN_H],
  });
  const miniOpacity = progress.interpolate({
    inputRange: [0, 0.4],
    outputRange: [1, 0],
    extrapolate: "clamp",
  });
  const expandedOpacity = progress.interpolate({
    inputRange: [0.5, 1],
    outputRange: [0, 1],
    extrapolate: "clamp",
  });

  const displayPosition = dragging ? sliderValue : positionMillis;
  const progressFraction = durationMillis ? Math.min(1, displayPosition / durationMillis) : 0;

  const handleDownload = async () => {
    if (isDownloaded(current.id) || downloading) return;
    setDownloading(true);
    try {
      const localUri = await downloadSongFile(current);
      addDownload({ ...current, localUri });
    } catch (e) {
      Alert.alert("Download failed", "Could not download this song. Please try again.");
    } finally {
      setDownloading(false);
    }
  };

  return (
    <>
      <Animated.View style={[styles.container, { bottom, height }]} {...panResponder.panHandlers}>
        {/* Mini bar */}
        <Animated.View
          style={[styles.miniWrap, { opacity: miniOpacity }]}
          pointerEvents={sheetExpanded ? "none" : "auto"}
        >
          <TouchableOpacity
            style={styles.miniTouchable}
            activeOpacity={0.85}
            onPress={() => setSheetExpanded(true)}
          >
            <Image source={{ uri: current.image }} style={styles.miniArt} />
            <View style={styles.miniText}>
              <Text style={styles.miniTitle} numberOfLines={1}>
                {current.name}
              </Text>
              <Text style={styles.miniArtist} numberOfLines={1}>
                {current.artist}
              </Text>
            </View>
            <TouchableOpacity onPress={togglePlayPause} style={styles.miniPlayBtn} hitSlop={8}>
              {isBuffering ? (
                <ActivityIndicator color={theme.accentOn} size="small" />
              ) : (
                <Ionicons name={isPlaying ? "pause" : "play"} size={18} color={theme.accentOn} />
              )}
            </TouchableOpacity>
            <TouchableOpacity onPress={playNext} style={styles.miniBtn} hitSlop={8}>
              <Ionicons name="play-skip-forward" size={18} color={theme.accent} />
            </TouchableOpacity>
          </TouchableOpacity>
          <View style={styles.miniProgressTrack}>
            <View style={[styles.miniProgressFill, { width: `${progressFraction * 100}%` }]} />
          </View>
        </Animated.View>

        {/* Expanded full player */}
        <Animated.View
          style={[
            styles.expandedWrap,
            { opacity: expandedOpacity, paddingTop: insets.top + 14 },
          ]}
          pointerEvents={sheetExpanded ? "auto" : "none"}
        >
          <TouchableOpacity onPress={() => setSheetExpanded(false)} hitSlop={10} style={styles.collapseBtn}>
            <Ionicons name="chevron-down" size={26} color={theme.text} />
          </TouchableOpacity>

          <View style={styles.bannerWrap}>
            <Image source={{ uri: current.image }} style={styles.banner} />
          </View>

          <View style={styles.infoWrap}>
            <Text style={styles.songName} numberOfLines={1}>
              {current.name}
            </Text>
            <Text style={styles.songArtist} numberOfLines={1}>
              {current.artist}
            </Text>
          </View>

          <View style={styles.progressRow}>
            <Text style={styles.timeText}>{formatMillis(displayPosition)}</Text>
            <Slider
              style={{ flex: 1, height: 32, marginHorizontal: 8 }}
              minimumValue={0}
              maximumValue={durationMillis || 1}
              value={displayPosition}
              minimumTrackTintColor={theme.accent}
              maximumTrackTintColor={theme.surfaceAlt}
              thumbTintColor={theme.accent}
              onSlidingStart={() => setDragging(true)}
              onValueChange={setSliderValue}
              onSlidingComplete={(v) => {
                setDragging(false);
                seekTo(v);
              }}
            />
            <Text style={styles.timeText}>{formatMillis(durationMillis)}</Text>
          </View>

          <View style={styles.controlsRow}>
            <TouchableOpacity onPress={playPrevious} hitSlop={10}>
              <Ionicons name="play-skip-back" size={28} color={theme.accent} />
            </TouchableOpacity>
            <TouchableOpacity onPress={togglePlayPause} style={styles.playBtn}>
              {isBuffering ? (
                <ActivityIndicator color={theme.accentOn} />
              ) : (
                <Ionicons
                  name={isPlaying ? "pause" : "play"}
                  size={28}
                  color={theme.accentOn}
                  style={{ marginLeft: isPlaying ? 0 : 3 }}
                />
              )}
            </TouchableOpacity>
            <TouchableOpacity onPress={playNext} hitSlop={10}>
              <Ionicons name="play-skip-forward" size={28} color={theme.accent} />
            </TouchableOpacity>
          </View>

          <View style={styles.actionsRow}>
            <TouchableOpacity style={styles.actionBtn} onPress={() => toggleFavorite(current)} hitSlop={8}>
              <View style={styles.actionIconWrap}>
                <Ionicons
                  name={isFavorite(current.id) ? "heart" : "heart-outline"}
                  size={20}
                  color={isFavorite(current.id) ? theme.accent : theme.textSecondary}
                />
              </View>
              <Text style={styles.actionLabel}>favourites</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.actionBtn} onPress={() => setShowAddToPlaylist(true)} hitSlop={8}>
              <View style={styles.actionIconWrap}>
                <Ionicons name="add" size={20} color={theme.textSecondary} />
              </View>
              <Text style={styles.actionLabel}>playlists</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.actionBtn} onPress={handleDownload} hitSlop={8}>
              <View style={styles.actionIconWrap}>
                {downloading ? (
                  <ActivityIndicator color={theme.textSecondary} size="small" />
                ) : (
                  <Ionicons
                    name={isDownloaded(current.id) ? "checkmark" : "download-outline"}
                    size={20}
                    color={isDownloaded(current.id) ? theme.success : theme.textSecondary}
                  />
                )}
              </View>
              <Text style={styles.actionLabel}>
                {isDownloaded(current.id) ? "saved" : "download"}
              </Text>
            </TouchableOpacity>
          </View>
        </Animated.View>
      </Animated.View>

      <AddToPlaylistModal
        visible={showAddToPlaylist}
        song={current}
        onClose={() => setShowAddToPlaylist(false)}
      />
    </>
  );
}

const makeStyles = (theme) =>
  StyleSheet.create({
    container: {
      position: "absolute",
      left: 0,
      right: 0,
      overflow: "hidden",
      backgroundColor: theme.background,
      zIndex: 50,
    },
    miniWrap: { ...StyleSheet.absoluteFillObject, borderTopWidth: 1, borderTopColor: theme.border },
    miniTouchable: { flex: 1, flexDirection: "row", alignItems: "center", paddingHorizontal: 14 },
    miniArt: { width: 44, height: 44, borderRadius: 10 },
    miniText: { flex: 1, marginLeft: 12 },
    miniTitle: { color: theme.text, fontSize: 14, fontWeight: "700" },
    miniArtist: { color: theme.textFaint, fontSize: 11.5, marginTop: 1 },
    miniPlayBtn: {
      width: 34,
      height: 34,
      borderRadius: 17,
      backgroundColor: theme.accent,
      alignItems: "center",
      justifyContent: "center",
      marginRight: 6,
    },
    miniBtn: { paddingHorizontal: 6, paddingVertical: 6 },
    miniProgressTrack: { height: 2, backgroundColor: theme.surfaceAlt },
    miniProgressFill: { height: 2, backgroundColor: theme.accent },

    expandedWrap: { flex: 1, paddingHorizontal: 24 },
    collapseBtn: { alignSelf: "flex-start", marginBottom: 10 },
    bannerWrap: { marginTop: 18, alignItems: "center" },
    banner: {
      width: SCREEN_W - 88,
      height: SCREEN_W - 88,
      borderRadius: 16,
      backgroundColor: theme.placeholder,
    },
    infoWrap: { marginTop: 26, width: "100%" },
    songName: { color: theme.text, fontSize: 22, fontWeight: "800" },
    songArtist: { color: theme.textSecondary, fontSize: 14, marginTop: 5 },
    progressRow: { flexDirection: "row", alignItems: "center", marginTop: 26 },
    timeText: { color: theme.textFaint, fontSize: 12 },
    controlsRow: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-evenly",
      marginTop: 18,
    },
    playBtn: {
      width: 68,
      height: 68,
      borderRadius: 34,
      backgroundColor: theme.accent,
      alignItems: "center",
      justifyContent: "center",
    },
    actionsRow: { flexDirection: "row", justifyContent: "space-evenly", marginTop: 32 },
    actionBtn: { alignItems: "center" },
    actionIconWrap: {
      width: 44,
      height: 44,
      borderRadius: 12,
      backgroundColor: theme.surface,
      alignItems: "center",
      justifyContent: "center",
    },
    actionLabel: { color: theme.textFaint, fontSize: 11, marginTop: 6 },
  });
