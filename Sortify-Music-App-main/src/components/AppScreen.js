import React from "react";
import { View, StatusBar } from "react-native";
import { useAppTheme } from "../context/ThemeContext";

// Flat, theme-aware screen background. Replaces the old blurred/gradient
// AppBackground now that the design is flat light/dark, not glassmorphism.
export default function AppScreen({ children, style }) {
  const { theme } = useAppTheme();
  return (
    <View style={[{ flex: 1, backgroundColor: theme.background }, style]}>
      <StatusBar barStyle={theme.statusBarStyle === "dark" ? "dark-content" : "light-content"} />
      {children}
    </View>
  );
}
