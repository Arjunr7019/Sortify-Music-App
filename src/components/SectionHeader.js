import React, { useMemo } from "react";
import { View, Text, StyleSheet, TouchableOpacity } from "react-native";
import { useAppTheme } from "../context/ThemeContext";

export default function SectionHeader({ title, subtitle, onPressSeeAll }) {
  const { theme } = useAppTheme();
  const styles = useMemo(() => makeStyles(theme), [theme]);

  return (
    <View style={styles.row}>
      <View>
        <Text style={styles.title}>{title}</Text>
        {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}
      </View>
      {onPressSeeAll ? (
        <TouchableOpacity onPress={onPressSeeAll} hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}>
          <Text style={styles.seeAll}>See all</Text>
        </TouchableOpacity>
      ) : null}
    </View>
  );
}

const makeStyles = (theme) =>
  StyleSheet.create({
    row: {
      flexDirection: "row",
      justifyContent: "space-between",
      alignItems: "flex-end",
      paddingHorizontal: 20,
      marginBottom: 12,
      marginTop: 22,
    },
    title: { color: theme.text, fontSize: 18, fontWeight: "800", letterSpacing: 0.2 },
    subtitle: { color: theme.textFaint, fontSize: 12, marginTop: 2 },
    seeAll: { color: theme.accent, fontSize: 13, fontWeight: "700" },
  });
