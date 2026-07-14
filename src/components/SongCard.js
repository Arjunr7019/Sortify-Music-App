import React from "react";
import { TouchableOpacity, Image, Text, StyleSheet, View } from "react-native";
import colors from "../theme/colors";

const SIZE = 128;

export default function SongCard({ song, onPress }) {
  return (
    <TouchableOpacity style={styles.wrap} onPress={onPress} activeOpacity={0.8}>
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

const styles = StyleSheet.create({
  wrap: { width: SIZE, marginRight: 14 },
  imageWrap: {
    width: SIZE,
    height: SIZE,
    borderRadius: 16,
    overflow: "hidden",
    borderWidth: 1,
    borderColor: colors.border,
  },
  image: { width: "100%", height: "100%" },
  placeholder: { backgroundColor: colors.surface },
  name: { color: colors.text, fontSize: 13, fontWeight: "600", marginTop: 8 },
  artist: { color: colors.textFaint, fontSize: 11, marginTop: 2 },
});
