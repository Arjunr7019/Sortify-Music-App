import React from "react";
import { TouchableOpacity, Text, View, StyleSheet } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { LinearGradient } from "expo-linear-gradient";
import colors from "../theme/colors";

export default function PlaylistCard({ playlist, onPress }) {
  const count = playlist.songs?.length || 0;
  return (
    <TouchableOpacity style={styles.wrap} onPress={onPress} activeOpacity={0.8}>
      <LinearGradient
        colors={colors.gradient}
        start={{ x: 0, y: 0 }}
        end={{ x: 1, y: 1 }}
        style={styles.artwork}
      >
        <Ionicons name="musical-notes" size={26} color="rgba(255,255,255,0.85)" />
      </LinearGradient>
      <Text style={styles.name} numberOfLines={1}>
        {playlist.name}
      </Text>
      <Text style={styles.count}>
        {count} {count === 1 ? "song" : "songs"}
      </Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  wrap: { width: 130, marginRight: 14 },
  artwork: {
    width: 130,
    height: 130,
    borderRadius: 16,
    alignItems: "center",
    justifyContent: "center",
    borderWidth: 1,
    borderColor: colors.border,
  },
  name: { color: colors.text, fontSize: 13, fontWeight: "600", marginTop: 8 },
  count: { color: colors.textFaint, fontSize: 11, marginTop: 2 },
});
