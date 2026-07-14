import React from "react";
import { View, Text, StyleSheet, TouchableOpacity } from "react-native";
import colors from "../theme/colors";

export default function SectionHeader({ title, subtitle, onPressSeeAll }) {
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

const styles = StyleSheet.create({
  row: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "flex-end",
    paddingHorizontal: 20,
    marginBottom: 12,
    marginTop: 22,
  },
  title: {
    color: colors.text,
    fontSize: 19,
    fontWeight: "700",
    letterSpacing: 0.2,
  },
  subtitle: {
    color: colors.textFaint,
    fontSize: 12,
    marginTop: 2,
  },
  seeAll: {
    color: colors.coral,
    fontSize: 13,
    fontWeight: "600",
  },
});
