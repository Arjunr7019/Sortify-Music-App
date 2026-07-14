import React from "react";
import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { LinearGradient } from "expo-linear-gradient";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import AppBackground from "../components/AppBackground";
import GlassView from "../components/GlassView";
import colors from "../theme/colors";
import { useLibrary } from "../context/LibraryContext";

const TILES = [
  { key: "Favorites", label: "Favorites", icon: "heart", desc: "Songs you love" },
  { key: "Playlists", label: "Playlist", icon: "albums", desc: "Your custom mixes" },
  { key: "Downloads", label: "Download", icon: "download", desc: "Saved for offline" },
];

export default function LibraryScreen({ navigation }) {
  const insets = useSafeAreaInsets();
  const { favorites, playlists, downloads } = useLibrary();

  const counts = {
    Favorites: favorites.length,
    Playlists: playlists.length,
    Downloads: downloads.length,
  };

  return (
    <AppBackground>
      <View style={[styles.header, { paddingTop: insets.top + 10 }]}>
        <Text style={styles.title}>Your Library</Text>
      </View>

      <View style={styles.list}>
        {TILES.map((tile) => (
          <TouchableOpacity
            key={tile.key}
            activeOpacity={0.8}
            onPress={() => navigation.navigate(tile.key)}
          >
            <GlassView style={styles.tile} radius={20}>
              <View style={styles.tileInner}>
                <LinearGradient colors={colors.gradient} style={styles.iconWrap}>
                  <Ionicons name={tile.icon} size={22} color="#fff" />
                </LinearGradient>
                <View style={{ flex: 1, marginLeft: 14 }}>
                  <Text style={styles.tileLabel}>{tile.label}</Text>
                  <Text style={styles.tileDesc}>{tile.desc}</Text>
                </View>
                <Text style={styles.tileCount}>{counts[tile.key]}</Text>
                <Ionicons
                  name="chevron-forward"
                  size={18}
                  color={colors.textFaint}
                  style={{ marginLeft: 6 }}
                />
              </View>
            </GlassView>
          </TouchableOpacity>
        ))}
      </View>
    </AppBackground>
  );
}

const styles = StyleSheet.create({
  header: { paddingHorizontal: 20, marginBottom: 20 },
  title: { color: colors.text, fontSize: 26, fontWeight: "800" },
  list: { paddingHorizontal: 20, gap: 14 },
  tile: { marginBottom: 14 },
  tileInner: { flexDirection: "row", alignItems: "center", padding: 16 },
  iconWrap: {
    width: 48,
    height: 48,
    borderRadius: 14,
    alignItems: "center",
    justifyContent: "center",
  },
  tileLabel: { color: colors.text, fontSize: 16, fontWeight: "700" },
  tileDesc: { color: colors.textFaint, fontSize: 12, marginTop: 2 },
  tileCount: { color: colors.textDim, fontSize: 14, fontWeight: "600" },
});
