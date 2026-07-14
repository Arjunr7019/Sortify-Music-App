import React from "react";
import { TouchableOpacity, Image, Text, View, StyleSheet } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import colors from "../theme/colors";

export default function SongListRow({ song, onPress, onPressMore, active }) {
  return (
    <TouchableOpacity style={styles.row} onPress={onPress} activeOpacity={0.75}>
      <View style={styles.imageWrap}>
        {song.image ? (
          <Image source={{ uri: song.image }} style={styles.image} />
        ) : (
          <View style={[styles.image, styles.placeholder]} />
        )}
      </View>
      <View style={styles.textWrap}>
        <Text
          style={[styles.name, active && { color: colors.coral }]}
          numberOfLines={1}
        >
          {song.name}
        </Text>
        <Text style={styles.artist} numberOfLines={1}>
          {song.artist}
        </Text>
      </View>
      {onPressMore ? (
        <TouchableOpacity onPress={onPressMore} hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}>
          <Ionicons name="ellipsis-vertical" size={18} color={colors.textDim} />
        </TouchableOpacity>
      ) : null}
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    alignItems: "center",
    paddingHorizontal: 20,
    paddingVertical: 8,
  },
  imageWrap: {
    width: 50,
    height: 50,
    borderRadius: 10,
    overflow: "hidden",
    borderWidth: 1,
    borderColor: colors.border,
  },
  image: { width: "100%", height: "100%" },
  placeholder: { backgroundColor: colors.surface },
  textWrap: { flex: 1, marginLeft: 12, marginRight: 8 },
  name: { color: colors.text, fontSize: 14.5, fontWeight: "600" },
  artist: { color: colors.textFaint, fontSize: 12, marginTop: 2 },
});
