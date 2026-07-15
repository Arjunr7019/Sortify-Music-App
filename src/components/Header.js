import React, { useMemo } from "react";
import { View, TouchableOpacity, StyleSheet } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useAppTheme } from "../context/ThemeContext";

// Shared top bar: hamburger (opens Settings) + avatar, matching the new
// flat mockups. Used on Home, Search, and Library.
export default function Header() {
  const insets = useSafeAreaInsets();
  const { theme, openSettings } = useAppTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);

  return (
    <View style={[styles.wrap, { paddingTop: insets.top + 14 }]}>
      <TouchableOpacity onPress={openSettings} hitSlop={10} style={styles.menuBtn}>
        <Ionicons name="menu" size={24} color={theme.text} />
      </TouchableOpacity>
      <TouchableOpacity onPress={openSettings} hitSlop={10}>
        <View style={styles.avatar}>
          <Ionicons name="person" size={16} color={theme.textSecondary} />
        </View>
      </TouchableOpacity>
    </View>
  );
}

const makeStyles = (theme) =>
  StyleSheet.create({
    wrap: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
      paddingHorizontal: 20,
      paddingBottom: 10,
    },
    menuBtn: { padding: 2 },
    avatar: {
      width: 34,
      height: 34,
      borderRadius: 17,
      backgroundColor: theme.surfaceAlt,
      alignItems: "center",
      justifyContent: "center",
    },
  });
