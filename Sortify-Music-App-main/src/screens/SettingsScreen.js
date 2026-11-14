import React, { useMemo, useState } from "react";
import { Modal, View, Text, TouchableOpacity, StyleSheet, Switch } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useAppTheme } from "../context/ThemeContext";
import { useOnboarding } from "../context/OnboardingContext";
import { useQuality, QUALITY_OPTIONS } from "../context/QualityContext";
import LanguagePickerModal from "../components/LanguagePickerModal";

export default function SettingsScreen() {
  const insets = useSafeAreaInsets();
  const { theme, mode, toggleMode, settingsOpen, closeSettings } = useAppTheme();
  const { languages } = useOnboarding();
  const { quality, setQuality } = useQuality();
  const styles = useMemo(() => makeStyles(theme), [theme]);
  const [showLanguages, setShowLanguages] = useState(false);

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
            <Ionicons name={mode === "dark" ? "moon" : "sunny"} size={18} color={theme.accent} />
          </View>
          <View style={{ flex: 1, marginLeft: 12 }}>
            <Text style={styles.rowLabel}>Dark mode</Text>
            <Text style={styles.rowDesc}>{mode === "dark" ? "Currently on" : "Currently off"}</Text>
          </View>
          <Switch
            value={mode === "dark"}
            onValueChange={toggleMode}
            trackColor={{ false: theme.surfaceAlt, true: theme.accent }}
            thumbColor="#FFFFFF"
          />
        </View>

        <Text style={styles.sectionLabel}>Audio quality</Text>
        <View style={styles.qualityRow}>
          {QUALITY_OPTIONS.map((q) => {
            const active = quality === q;
            return (
              <TouchableOpacity
                key={q}
                style={[styles.qualityChip, active && { backgroundColor: theme.accent }]}
                onPress={() => setQuality(q)}
                activeOpacity={0.75}
              >
                <Text style={[styles.qualityText, active && { color: theme.accentOn, fontWeight: "700" }]}>
                  {q}
                </Text>
              </TouchableOpacity>
            );
          })}
        </View>
        <Text style={styles.hint}>Higher bitrates sound better but use more data.</Text>

        <Text style={styles.sectionLabel}>Trending languages</Text>
        <TouchableOpacity style={styles.row} onPress={() => setShowLanguages(true)} activeOpacity={0.75}>
          <View style={styles.rowIcon}>
            <Ionicons name="language" size={18} color={theme.accent} />
          </View>
          <View style={{ flex: 1, marginLeft: 12 }}>
            <Text style={styles.rowLabel} numberOfLines={1}>
              {languages.length ? languages.join(", ") : "None selected"}
            </Text>
            <Text style={styles.rowDesc}>Used for your Home trending rows</Text>
          </View>
          <Ionicons name="chevron-forward" size={16} color={theme.textFaint} />
        </TouchableOpacity>

        <Text style={styles.footer}>Sortify</Text>
      </View>

      <LanguagePickerModal visible={showLanguages} onClose={() => setShowLanguages(false)} />
    </Modal>
  );
}

const makeStyles = (theme) =>
  StyleSheet.create({
    root: { flex: 1, backgroundColor: theme.background, paddingHorizontal: 20 },
    header: { flexDirection: "row", alignItems: "center", justifyContent: "space-between", marginBottom: 24 },
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
    qualityRow: { flexDirection: "row", gap: 10 },
    qualityChip: {
      flex: 1,
      paddingVertical: 12,
      borderRadius: 12,
      backgroundColor: theme.surface,
      alignItems: "center",
    },
    qualityText: { color: theme.textSecondary, fontSize: 13, fontWeight: "600" },
    hint: { color: theme.textFaint, fontSize: 11.5, marginTop: 8 },
    footer: { color: theme.textFaint, fontSize: 12, textAlign: "center", marginTop: "auto", marginBottom: 24 },
  });
