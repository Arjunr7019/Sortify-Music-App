import React, { useMemo } from "react";
import { TouchableOpacity, Image, Text, StyleSheet, View } from "react-native";
import { useAppTheme } from "../context/ThemeContext";

const SIZE = 128;

export default function SongCard({ song, onPress }) {
  const { theme } = useAppTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);

  return (
    <TouchableOpacity style={styles.wrap} onPress={onPress} activeOpacity={0.75}>
      <View style={styles.imageWrap}>
        {song.image ? (
          <Image source={{ uri: song.image }} style={styles.image} />
        ) : (
          <View style={[styles.image, styles.placeholder]} />
        )}
      </View>
      <Text style={styles.name} numberOfLines={1}>
        {song.name}
      </Text>
      <Text style={styles.artist} numberOfLines={1}>
        {song.artist}
      </Text>
    </TouchableOpacity>
  );
}

const makeStyles = (theme) =>
  StyleSheet.create({
    wrap: { width: SIZE, marginRight: 14 },
    imageWrap: { width: SIZE, height: SIZE, borderRadius: 14, overflow: "hidden" },
    image: { width: "100%", height: "100%" },
    placeholder: { backgroundColor: theme.placeholder },
    name: { color: theme.text, fontSize: 13, fontWeight: "700", marginTop: 8 },
    artist: { color: theme.textFaint, fontSize: 11, marginTop: 2 },
  });
