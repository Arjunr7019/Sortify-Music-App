import React, { useMemo } from "react";
import { View, TouchableOpacity, Text, StyleSheet } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useAppTheme } from "../context/ThemeContext";

const ICONS = { Home: "home", Search: "search", LibraryTab: "library" };
const OUTLINE_ICONS = { Home: "home-outline", Search: "search-outline", LibraryTab: "library-outline" };
const LABELS = { Home: "Home", Search: "Search", LibraryTab: "Library" };

export default function BottomTabBar({ state, navigation }) {
  const insets = useSafeAreaInsets();
  const { theme } = useAppTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);

  return (
    <View style={[styles.wrap, { paddingBottom: insets.bottom || 10 }]}>
      {state.routes.map((route, index) => {
        const focused = state.index === index;
        const onPress = () => {
          if (!focused) navigation.navigate(route.name);
        };
        return (
          <TouchableOpacity key={route.key} style={styles.tab} onPress={onPress} activeOpacity={0.75}>
            <View style={[styles.iconChip, focused && { backgroundColor: theme.accent }]}>
              <Ionicons
                name={focused ? ICONS[route.name] : OUTLINE_ICONS[route.name]}
                size={18}
                color={focused ? theme.accentOn : theme.textFaint}
              />
            </View>
            <Text style={[styles.label, focused && { color: theme.accent, fontWeight: "700" }]}>
              {LABELS[route.name]}
            </Text>
          </TouchableOpacity>
        );
      })}
    </View>
  );
}

const makeStyles = (theme) =>
  StyleSheet.create({
    wrap: {
      flexDirection: "row",
      backgroundColor: theme.background,
      borderTopWidth: 1,
      borderTopColor: theme.border,
      paddingTop: 10,
    },
    tab: { flex: 1, alignItems: "center", justifyContent: "center" },
    iconChip: {
      width: 34,
      height: 30,
      borderRadius: 9,
      backgroundColor: theme.surfaceAlt,
      alignItems: "center",
      justifyContent: "center",
    },
    label: { color: theme.textFaint, fontSize: 11, marginTop: 4 },
  });
