import React, { useMemo } from "react";
import { View, Text, TouchableOpacity, StyleSheet } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import AppScreen from "../components/AppScreen";
import Header from "../components/Header";
import { useAppTheme } from "../context/ThemeContext";
import { useLibrary } from "../context/LibraryContext";

const TILES = [
  { key: "Favorites", label: "Favourites", icon: "heart" },
  { key: "Playlists", label: "Playlists", icon: "albums" },
  { key: "Downloads", label: "Downloads", icon: "download" },
];

export default function LibraryScreen({ navigation }) {
  const { theme } = useAppTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);
  const { favorites, playlists, downloads } = useLibrary();

  const counts = {
    Favorites: favorites.length,
    Playlists: playlists.length,
    Downloads: downloads.length,
  };

  return (
    <AppScreen>
      <Header />
      <Text style={styles.title}>Library</Text>

      <View style={styles.list}>
        {TILES.map((tile) => (
          <TouchableOpacity
            key={tile.key}
            activeOpacity={0.7}
            style={styles.row}
            onPress={() => navigation.navigate(tile.key)}
          >
            <View style={styles.iconWrap}>
              <Ionicons name={tile.icon} size={18} color={theme.accent} />
            </View>
            <Text style={styles.label}>{tile.label}</Text>
            <Text style={styles.count}>{counts[tile.key]}</Text>
            <Ionicons name="chevron-forward" size={16} color={theme.textFaint} style={{ marginLeft: 6 }} />
          </TouchableOpacity>
        ))}
      </View>
    </AppScreen>
  );
}

const makeStyles = (theme) =>
  StyleSheet.create({
    title: { color: theme.text, fontSize: 22, fontWeight: "800", textAlign: "center", marginBottom: 24 },
    list: { paddingHorizontal: 20 },
    row: {
      flexDirection: "row",
      alignItems: "center",
      paddingVertical: 14,
      borderBottomWidth: 1,
      borderBottomColor: theme.divider,
    },
    iconWrap: {
      width: 32,
      height: 32,
      borderRadius: 9,
      backgroundColor: theme.surfaceAlt,
      alignItems: "center",
      justifyContent: "center",
    },
    label: { color: theme.text, fontSize: 15, fontWeight: "600", marginLeft: 14, flex: 1 },
    count: { color: theme.textFaint, fontSize: 13, fontWeight: "600" },
  });
