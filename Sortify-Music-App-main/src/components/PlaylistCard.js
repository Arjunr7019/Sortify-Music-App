import React, { useMemo } from "react";
import { TouchableOpacity, Text, View, Image, StyleSheet } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useAppTheme } from "../context/ThemeContext";

// 2x2 collage of the playlist's first four song thumbnails, matching the
// "banner 1/2/3/4" grid in the new mockups. Falls back to a single icon
// tile when the playlist is empty.
export default function PlaylistCard({ playlist, onPress }) {
  const { theme } = useAppTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);
  const count = playlist.songs?.length || 0;
  const thumbs = (playlist.songs || []).slice(0, 4);

  return (
    <TouchableOpacity style={styles.wrap} onPress={onPress} activeOpacity={0.75}>
      {thumbs.length > 0 ? (
        <View style={styles.grid}>
          {[0, 1, 2, 3].map((i) => (
            <View key={i} style={styles.cell}>
              {thumbs[i]?.image ? (
                <Image source={{ uri: thumbs[i].image }} style={styles.cellImage} />
              ) : (
                <View style={[styles.cellImage, styles.cellPlaceholder]} />
              )}
            </View>
          ))}
        </View>
      ) : (
        <View style={styles.emptyArt}>
          <Ionicons name="musical-notes" size={26} color={theme.textFaint} />
        </View>
      )}
      <Text style={styles.name} numberOfLines={1}>
        {playlist.name}
      </Text>
      <Text style={styles.count}>
        {count} {count === 1 ? "song" : "songs"}
      </Text>
    </TouchableOpacity>
  );
}

const SIZE = 130;

const makeStyles = (theme) =>
  StyleSheet.create({
    wrap: { width: SIZE, marginRight: 14 },
    grid: {
      width: SIZE,
      height: SIZE,
      borderRadius: 14,
      overflow: "hidden",
      flexDirection: "row",
      flexWrap: "wrap",
    },
    cell: { width: SIZE / 2, height: SIZE / 2 },
    cellImage: { width: "100%", height: "100%" },
    cellPlaceholder: { backgroundColor: theme.placeholder },
    emptyArt: {
      width: SIZE,
      height: SIZE,
      borderRadius: 14,
      backgroundColor: theme.surface,
      alignItems: "center",
      justifyContent: "center",
    },
    name: { color: theme.text, fontSize: 13, fontWeight: "700", marginTop: 8 },
    count: { color: theme.textFaint, fontSize: 11, marginTop: 2 },
  });
