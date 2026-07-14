import React from "react";
import { View, StyleSheet, Dimensions } from "react-native";
import { LinearGradient } from "expo-linear-gradient";
import colors from "../theme/colors";

const { width: W, height: H } = Dimensions.get("window");

export default function AppBackground({ children }) {
  return (
    <View style={styles.root}>
      <LinearGradient colors={[colors.bgTop, colors.bgBottom]} style={StyleSheet.absoluteFill} />
      <View style={[styles.glow, styles.glowPink]} />
      <View style={[styles.glow, styles.glowAmber]} />
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: colors.bgBottom },
  glow: {
    position: "absolute",
    borderRadius: 999,
    opacity: 0.28,
  },
  glowPink: {
    width: W * 0.9,
    height: W * 0.9,
    backgroundColor: colors.pink,
    top: -W * 0.4,
    left: -W * 0.3,
  },
  glowAmber: {
    width: W * 0.8,
    height: W * 0.8,
    backgroundColor: colors.amber,
    bottom: -W * 0.35,
    right: -W * 0.3,
    opacity: 0.18,
  },
});
