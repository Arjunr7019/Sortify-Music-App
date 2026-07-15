import React, { useMemo } from "react";
import { Modal, View, Text, TouchableOpacity, StyleSheet, Switch } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useAppTheme } from "../context/ThemeContext";
import { useOnboarding } from "../context/OnboardingContext";

export default function SettingsScreen() {
  const insets = useSafeAreaInsets();
  const { theme, mode, toggleMode, settingsOpen, closeSettings } = useAppTheme();
  const { languages } = useOnboarding();
  const styles = useMemo(() => makeStyles(theme), [theme]);

  return (
    <Modal
      visible={settingsOpen}
      animationType="slide"
      onRequestClose={closeSettings}
      presentationStyle="pageSheet"
    >
      <View style={[styles.root, { paddingTop: insets.top + 16 }]}>
        <View style={styles.header}>
          <Text style={styles.title}>Settings</Text>
          <TouchableOpacity onPress={closeSettings} hitSlop={10}>
            <Ionicons name="close" size={24} color={theme.text} />
          </TouchableOpacity>
        </View>

        <Text style={styles.sectionLabel}>Appearance</Text>
        <View style={styles.row}>
          <View style={styles.rowIcon}>
            <Ionicons
              name={mode === "dark" ? "moon" : "sunny"}
              size={18}
              color={theme.accent}
            />
          </View>
          <View style={{ flex: 1, marginLeft: 12 }}>
            <Text style={styles.rowLabel}>Dark mode</Text>
            <Text style={styles.rowDesc}>
              {mode === "dark" ? "Currently on" : "Currently off"}
            </Text>
          </View>
          <Switch
            value={mode === "dark"}
            onValueChange={toggleMode}
            trackColor={{ false: theme.surfaceAlt, true: theme.accent }}
            thumbColor="#FFFFFF"
          />
        </View>

        <Text style={styles.sectionLabel}>Trending languages</Text>
        <View style={styles.row}>
          <View style={styles.rowIcon}>
            <Ionicons name="language" size={18} color={theme.accent} />
          </View>
          <View style={{ flex: 1, marginLeft: 12 }}>
            <Text style={styles.rowLabel}>
              {languages.length ? languages.join(", ") : "None selected"}
            </Text>
            <Text style={styles.rowDesc}>Used for your Home trending rows</Text>
          </View>
        </View>

        <Text style={styles.footer}>Sortify</Text>
      </View>
    </Modal>
  );
}

const makeStyles = (theme) =>
  StyleSheet.create({
    root: { flex: 1, backgroundColor: theme.background, paddingHorizontal: 20 },
    header: {
      flexDirection: "row",
      alignItems: "center",
      justifyContent: "space-between",
      marginBottom: 24,
    },
    title: { color: theme.text, fontSize: 22, fontWeight: "800" },
    sectionLabel: {
      color: theme.textFaint,
      fontSize: 11,
      fontWeight: "700",
      letterSpacing: 1,
      textTransform: "uppercase",
      marginBottom: 10,
      marginTop: 18,
    },
    row: {
      flexDirection: "row",
      alignItems: "center",
      backgroundColor: theme.surface,
      borderRadius: 14,
      paddingVertical: 14,
      paddingHorizontal: 14,
      borderWidth: 1,
      borderColor: theme.border,
    },
    rowIcon: {
      width: 36,
      height: 36,
      borderRadius: 10,
      backgroundColor: theme.surfaceAlt,
      alignItems: "center",
      justifyContent: "center",
    },
    rowLabel: { color: theme.text, fontSize: 14.5, fontWeight: "600" },
    rowDesc: { color: theme.textFaint, fontSize: 12, marginTop: 2 },
    footer: {
      color: theme.textFaint,
      fontSize: 12,
      textAlign: "center",
      marginTop: "auto",
      marginBottom: 24,
    },
  });
