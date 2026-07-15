import React, { useRef, useState, useEffect } from "react";
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
  Platform,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import Slider from "@react-native-community/slider";
import { BlurView } from "expo-blur";
import { LinearGradient } from "expo-linear-gradient";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import colors from "../theme/colors";
import { GLASS_BG, GLASS_BORDER, GLASS_BLUR_INTENSITY, glassShadow } from "../theme/glass";
import { usePlayer } from "../context/PlayerContext";
import { useLibrary } from "../context/LibraryContext";
import { formatMillis } from "../utils/format";
import { downloadSongFile } from "../utils/download";
import AddToPlaylistModal from "./AddToPlaylistModal";

const { height: SCREEN_H, width: SCREEN_W } = Dimensions.get("window");
const MINI_HEIGHT = 64;
const TAB_BAR_HEIGHT = 62;

export default function PlayerSheet() {
  const insets = useSafeAreaInsets();
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
    outputRange: [TAB_BAR_HEIGHT + 8, 0],
  });
  const height = progress.interpolate({
    inputRange: [0, 1],
    outputRange: [MINI_HEIGHT, SCREEN_H],
  });
  const radius = progress.interpolate({
    inputRange: [0, 1],
    outputRange: [18, 0],
  });
  const horizontalMargin = progress.interpolate({
    inputRange: [0, 1],
    outputRange: [12, 0],
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
      <Animated.View
        style={[
          styles.shadowWrap,
          {
            bottom,
            height,
            left: horizontalMargin,
            right: horizontalMargin,
          },
        ]}
      >
        <Animated.View
          style={[styles.container, { borderRadius: radius }]}
          {...panResponder.panHandlers}
        >
          <BlurView
            intensity={GLASS_BLUR_INTENSITY}
            tint="light"
            style={[
              StyleSheet.absoluteFill,
              Platform.OS === "android" && { backgroundColor: "rgba(255,255,255,0.16)" },
            ]}
          >
            <View style={[StyleSheet.absoluteFill, { backgroundColor: GLASS_BG }]} />
            <LinearGradient
              colors={["rgba(229,30,124,0.16)", "rgba(240,96,63,0.10)", "rgba(251,181,49,0.08)"]}
              style={StyleSheet.absoluteFill}
            />
        </BlurView>

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
            <TouchableOpacity onPress={togglePlayPause} style={styles.miniBtn} hitSlop={8}>
              {isBuffering ? (
                <ActivityIndicator color={colors.text} size="small" />
              ) : (
                <Ionicons name={isPlaying ? "pause" : "play"} size={20} color={colors.text} />
              )}
            </TouchableOpacity>
            <TouchableOpacity onPress={playNext} style={styles.miniBtn} hitSlop={8}>
              <Ionicons name="play-skip-forward" size={18} color={colors.text} />
            </TouchableOpacity>
          </TouchableOpacity>
        </Animated.View>

        {/* Expanded full player */}
        <Animated.View
          style={[
            styles.expandedWrap,
            { opacity: expandedOpacity, paddingTop: insets.top + 10 },
          ]}
          pointerEvents={sheetExpanded ? "auto" : "none"}
        >
          <View style={styles.dragHandle} />

          <View style={styles.expandedHeader}>
            <TouchableOpacity onPress={() => setSheetExpanded(false)} hitSlop={10}>
              <Ionicons name="chevron-down" size={26} color={colors.text} />
            </TouchableOpacity>
            <Text style={styles.nowPlayingLabel}>NOW PLAYING</Text>
            <View style={{ width: 26 }} />
          </View>

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

          <View style={styles.progressWrap}>
            <Slider
              style={{ width: "100%", height: 32 }}
              minimumValue={0}
              maximumValue={durationMillis || 1}
              value={displayPosition}
              minimumTrackTintColor={colors.coral}
              maximumTrackTintColor={colors.border}
              thumbTintColor={colors.amber}
              onSlidingStart={() => setDragging(true)}
              onValueChange={setSliderValue}
              onSlidingComplete={(v) => {
                setDragging(false);
                seekTo(v);
              }}
            />
            <View style={styles.timeRow}>
              <Text style={styles.timeText}>{formatMillis(displayPosition)}</Text>
              <Text style={styles.timeText}>{formatMillis(durationMillis)}</Text>
            </View>
          </View>

          <View style={styles.controlsRow}>
            <TouchableOpacity onPress={playPrevious} hitSlop={10}>
              <Ionicons name="play-skip-back" size={30} color={colors.text} />
            </TouchableOpacity>
            <TouchableOpacity onPress={togglePlayPause} style={styles.playBtn}>
              <LinearGradient colors={colors.gradient} style={styles.playBtnGradient}>
                {isBuffering ? (
                  <ActivityIndicator color="#fff" />
                ) : (
                  <Ionicons
                    name={isPlaying ? "pause" : "play"}
                    size={30}
                    color="#fff"
                    style={{ marginLeft: isPlaying ? 0 : 3 }}
                  />
                )}
              </LinearGradient>
            </TouchableOpacity>
            <TouchableOpacity onPress={playNext} hitSlop={10}>
              <Ionicons name="play-skip-forward" size={30} color={colors.text} />
            </TouchableOpacity>
          </View>

          <View style={styles.actionsRow}>
            <TouchableOpacity
              style={styles.actionBtn}
              onPress={() => toggleFavorite(current)}
              hitSlop={8}
            >
              <Ionicons
                name={isFavorite(current.id) ? "heart" : "heart-outline"}
                size={24}
                color={isFavorite(current.id) ? colors.pink : colors.textDim}
              />
              <Text style={styles.actionLabel}>Favorite</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.actionBtn}
              onPress={() => setShowAddToPlaylist(true)}
              hitSlop={8}
            >
              <Ionicons name="add-circle-outline" size={24} color={colors.textDim} />
              <Text style={styles.actionLabel}>Playlist</Text>
            </TouchableOpacity>

            <TouchableOpacity style={styles.actionBtn} onPress={handleDownload} hitSlop={8}>
              {downloading ? (
                <ActivityIndicator color={colors.textDim} />
              ) : (
                <Ionicons
                  name={isDownloaded(current.id) ? "checkmark-circle" : "download-outline"}
                  size={24}
                  color={isDownloaded(current.id) ? colors.success : colors.textDim}
                />
              )}
              <Text style={styles.actionLabel}>
                {isDownloaded(current.id) ? "Saved" : "Download"}
              </Text>
            </TouchableOpacity>
          </View>
        </Animated.View>
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

const styles = StyleSheet.create({
  shadowWrap: {
    position: "absolute",
    zIndex: 50,
    ...glassShadow,
  },
  container: {
    flex: 1,
    overflow: "hidden",
    borderWidth: 1,
    borderColor: GLASS_BORDER,
  },
  miniWrap: { ...StyleSheet.absoluteFillObject },
  miniTouchable: {
    flex: 1,
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 10,
  },
  miniArt: { width: 44, height: 44, borderRadius: 10 },
  miniText: { flex: 1, marginLeft: 10 },
  miniTitle: { color: colors.text, fontSize: 13.5, fontWeight: "700" },
  miniArtist: { color: colors.textFaint, fontSize: 11, marginTop: 1 },
  miniBtn: { paddingHorizontal: 8, paddingVertical: 6 },

  expandedWrap: { flex: 1, paddingHorizontal: 24 },
  dragHandle: {
    alignSelf: "center",
    width: 40,
    height: 4,
    borderRadius: 2,
    backgroundColor: colors.borderStrong,
    marginBottom: 6,
  },
  expandedHeader: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    marginTop: 10,
  },
  nowPlayingLabel: {
    color: colors.textFaint,
    fontSize: 12,
    fontWeight: "700",
    letterSpacing: 2,
  },
  bannerWrap: {
    marginTop: 28,
    alignItems: "center",
  },
  banner: {
    width: SCREEN_W - 88,
    height: SCREEN_W - 88,
    borderRadius: 24,
    borderWidth: 1,
    borderColor: colors.border,
  },
  infoWrap: { marginTop: 28, alignItems: "center" },
  songName: { color: colors.text, fontSize: 21, fontWeight: "700", maxWidth: "90%" },
  songArtist: { color: colors.textDim, fontSize: 14, marginTop: 6 },
  progressWrap: { marginTop: 22 },
  timeRow: { flexDirection: "row", justifyContent: "space-between", marginTop: -4 },
  timeText: { color: colors.textFaint, fontSize: 11.5 },
  controlsRow: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-evenly",
    marginTop: 20,
  },
  playBtn: { borderRadius: 40, overflow: "hidden" },
  playBtnGradient: {
    width: 72,
    height: 72,
    borderRadius: 36,
    alignItems: "center",
    justifyContent: "center",
  },
  actionsRow: {
    flexDirection: "row",
    justifyContent: "space-evenly",
    marginTop: 30,
  },
  actionBtn: { alignItems: "center" },
  actionLabel: { color: colors.textFaint, fontSize: 11, marginTop: 6 },
});
