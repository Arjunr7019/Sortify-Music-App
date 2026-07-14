import React from "react";
import { StyleSheet, View, Platform } from "react-native";
import { BlurView } from "expo-blur";
import colors from "../theme/colors";

/**
 * Standard glassmorphism container used across Sortify.
 * intensity: blur strength, tint: 'dark' | 'light'
 */
export default function GlassView({
  children,
  style,
  intensity = 40,
  radius = 20,
  border = true,
  contentStyle,
}) {
  return (
    <View style={[{ borderRadius: radius, overflow: "hidden" }, style]}>
      <BlurView
        intensity={intensity}
        tint="dark"
        style={[
          styles.blur,
          {
            borderRadius: radius,
            borderWidth: border ? 1 : 0,
            borderColor: colors.border,
          },
          contentStyle,
        ]}
      >
        {children}
      </BlurView>
    </View>
  );
}

const styles = StyleSheet.create({
  blur: {
    backgroundColor: Platform.OS === "android" ? "rgba(30,14,36,0.55)" : "rgba(255,255,255,0.06)",
    padding: 0,
  },
});
