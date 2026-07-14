import React from "react";
import { View, TouchableOpacity, Text, StyleSheet } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import GlassView from "../components/GlassView";
import colors from "../theme/colors";

const ICONS = {
  Home: "home",
  Search: "search",
  LibraryTab: "library",
};
const OUTLINE_ICONS = {
  Home: "home-outline",
  Search: "search-outline",
  LibraryTab: "library-outline",
};
const LABELS = {
  Home: "Home",
  Search: "Search",
  LibraryTab: "Library",
};

export default function GlassTabBar({ state, navigation }) {
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.wrap, { bottom: insets.bottom > 0 ? insets.bottom - 6 : 8 }]}>
      <GlassView radius={22} intensity={55} style={styles.bar}>
        <View style={styles.row}>
          {state.routes.map((route, index) => {
            const focused = state.index === index;
            const onPress = () => {
              if (!focused) navigation.navigate(route.name);
            };
            return (
              <TouchableOpacity key={route.key} style={styles.tab} onPress={onPress} activeOpacity={0.75}>
                <Ionicons
                  name={focused ? ICONS[route.name] : OUTLINE_ICONS[route.name]}
                  size={22}
                  color={focused ? colors.coral : colors.textFaint}
                />
                <Text style={[styles.label, focused && { color: colors.coral, fontWeight: "700" }]}>
                  {LABELS[route.name]}
                </Text>
              </TouchableOpacity>
            );
          })}
        </View>
      </GlassView>
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: { position: "absolute", left: 16, right: 16 },
  bar: {},
  row: { flexDirection: "row", paddingVertical: 10 },
  tab: { flex: 1, alignItems: "center", justifyContent: "center" },
  label: { color: colors.textFaint, fontSize: 11, marginTop: 3 },
});
